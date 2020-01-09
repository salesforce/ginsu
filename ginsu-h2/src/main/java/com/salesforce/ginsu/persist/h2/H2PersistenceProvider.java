/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persist.h2;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.salesforce.ginsu.persistence.*;
import com.salesforce.ginsu.schema.ColumnType;
import com.salesforce.ginsu.warehouse.SurrogateKey;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public class H2PersistenceProvider implements PersistenceProvider {

    // NOTE: There doesn't seem to be any advantage to using more compact key types for low-cardinality dimensions
    private static final String SURROGATE_KEY_TYPE = "INT";

    private final ThreadLocalConnectionPool connections;
    private final int batchSize;
    private final Logger logger;


    private final ListMultimap<TableDef, Row> batchedRows = ArrayListMultimap.create();

    H2PersistenceProvider(String connectionString, String username, String password, int batchSize, Logger logger) {
        this.batchSize = batchSize;
        this.logger = requireNonNull(logger);
        this.connections = new ThreadLocalConnectionPool(connectionString, username, password, logger);
    }


    @Override
    public void validateSchema(final ValidateSchemaRequest request) throws PersistenceException {
        logger.warning("validateSchema not yet implemented - FIXME");
    }

    @Override
    public void createSchema(final CreateSchemaRequest request) throws PersistenceException {
        try (final Statement stmt = connections.get().createStatement()) {
            for (final TableDef table : request.getTables()) {
                createTable(stmt, table);
                for (final ColumnDef col : table.getValueColumns()) {
                    if (!col.needsIndex()) {
                        logger.warning("creating index on " + col.getColumnName() + " anyway");

                    }
                    final boolean useHashIndex = col.getType() == ColumnType.FOREIGN_KEY;
                    createIndex(stmt, table, col.getColumnName(), useHashIndex);
                }
                if (table.getTableName().endsWith("_FACT")) { //FIXME
                    createView(stmt, table);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public SurrogateKey insert(final InsertMode mode, final TableDef table, final Row values) {
        try {
            switch (mode) {
                case INSERT:
                case INSERT_ANONYMOUS:
                    return insert(table, values);
                case UPSERT:
                case UPSERT_ANONYMOUS:
                    return upsert(table, values);
                default:
                    throw new IllegalStateException("bad mode " + mode);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            logger.info("H2 shutdown: Flushing batched inserts");
            flushBatches();
            logger.info("H2 shutdown: Executing SHUTDOWN DEFRAG");
            try (final Statement stmt = connections.get().createStatement()) {
                stmt.execute("SHUTDOWN DEFRAG");
                logger.info("H2 shutdown: Complete.");
            }
        } catch (SQLException sqe) {
            throw new IOException(sqe);
        } finally {
            connections.close();
        }
    }

    private SurrogateKey upsert(final TableDef table, final Row values) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ");
        sql.append(table.getTableName());
        sql.append(" (");
        for (final ColumnDef col : table.getValueColumns()) {
            sql.append(col.getColumnName());
            sql.append(",");
        }
        sql.append(") KEY (");
        for (final ColumnDef col : table.getValueColumns()) {
            sql.append(col.getColumnName());
            sql.append(",");
        }
        sql.append(") VALUES (");
        for (final ColumnDef col : table.getValueColumns()) {
            sql.append("?, ");
        }
        sql.append(")");
        try (final PreparedStatement stmt = connections.get().prepareStatement(sql.toString())) {
            logger.fine("executing merge: " + sql);
            int paramIndex = 1;
            for (final ColumnDef col : table.getValueColumns()) {
                stmt.setObject(paramIndex++, prepareValue(col, values.getValue(col)));
            }
            stmt.executeUpdate();
            if (table.hasSurrogateKey()) { //FIXME need a 'blindInsert' method
                return querySurrogateKey(table, values);
            } else {
                return null;
            }
        }
    }

    private SurrogateKey querySurrogateKey(final TableDef table, final Row values) throws SQLException {
        if (!table.hasSurrogateKey()) {
            throw new IllegalArgumentException(table.getTableName() + " does not have a surrogate key");
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(table.getSurrogateKeyColumnName());
        sql.append(" FROM ");
        sql.append(table.getTableName());
        sql.append(" WHERE ");
        {
            int paramIndex = 1;
            for (final ColumnDef col : table.getValueColumns()) {
                if (paramIndex++ > 1) sql.append(" AND ");
                sql.append(col.getColumnName());
                sql.append(" = ?");
            }
        }
        try (final PreparedStatement stmt = connections.get().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (final ColumnDef col : table.getValueColumns()) {
                stmt.setObject(paramIndex++, prepareValue(col, values.getValue(col)));
            }
            final ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new IllegalStateException("no next row after executing query " + sql);
            }
            return new LongSurrogateKey(rs.getLong(1));
        }
    }

    private Object prepareValue(final ColumnDef col, final Object value) {
        if (value instanceof LongSurrogateKey) {
            return ((LongSurrogateKey) value).getKeyValue();
        } else if (value instanceof String) {
            if (((String) value).length() > 254) {
                return ((String) value).substring(0, 255);
            }
        }
        return value;
    }

    private SurrogateKey insert(TableDef table, Row values) throws SQLException {
        if (this.batchSize > 1 && !table.hasSurrogateKey()) {
            batchInsert(table, values);
            return null;
        }
        final String sql = buildInsertSql(table);
        try (final PreparedStatement stmt = connections.get().prepareStatement(sql)) {
            logger.fine("executing merge: " + sql);
            int paramIndex = 1;
            for (final ColumnDef col : table.getValueColumns()) {
                stmt.setObject(paramIndex++, prepareValue(col, values.getValue(col)));
            }
            stmt.executeUpdate();
            if (table.hasSurrogateKey()) {
                return querySurrogateKey(table, values);
            } else {
                return null;
            }
        }
    }


    private String buildInsertSql(TableDef table) {
        final StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(table.getTableName());
        sql.append(" (");
        for (final ColumnDef col : table.getValueColumns()) {
            sql.append(col.getColumnName());
            sql.append(",");
        }
        sql.append(") VALUES (");
        for (final ColumnDef col : table.getValueColumns()) {
            sql.append("?, ");
        }
        sql.append(")");
        return sql.toString();
    }

    private synchronized void batchInsert(TableDef table, Row row) throws SQLException {
        batchedRows.put(table, row);
        if (batchedRows.get(table).size() < batchSize) return;
        logger.info(() -> {
            return "==== Executing batch insert on " + table.getTableName();
        });
        final List<Row> rowsToInsert = batchedRows.removeAll(table);
        doBatch(table, rowsToInsert);
    }

    private synchronized void doBatch(TableDef table, List<Row> rowsToInsert) throws SQLException {
        final String sql = buildInsertSql(table);
        try (final PreparedStatement stmt = connections.get().prepareStatement(sql)) {
            for (final Row rowToInsert : rowsToInsert) {
                int paramIndex = 1;
                for (final ColumnDef col : table.getValueColumns()) {
                    stmt.setObject(paramIndex++, prepareValue(col, rowToInsert.getValue(col)));
                }
                stmt.addBatch();
                stmt.clearParameters();
            }
            stmt.executeBatch();
        }
    }

    private synchronized void flushBatches() throws SQLException {
        for (TableDef table : this.batchedRows.keySet()) {
            logger.info("Flushing batched inserts to " + table.getTableName());
            List<Row> remainingRows = this.batchedRows.removeAll(table);
            if (remainingRows != null) {
                doBatch(table, remainingRows);
            }
        }
    }

    private void createView(final Statement stmt, final TableDef table) throws SQLException {
        final String sql = buildCreateViewSql(table);
        logger.info("creating view: " + sql);
        stmt.executeUpdate(sql);
        logger.info("created view: " + table.getName());

    }

    private String buildCreateViewSql(TableDef table) {
        final StringBuilder sql = new StringBuilder();
        sql.append("CREATE VIEW ");
        sql.append(table.getName()); //FIXME
        sql.append(" AS SELECT ");
        int index = 1;
        for (final ColumnDef column : table.getValueColumns()) {
            final TableDef refTable = column.getReferencedTable();
            if (refTable != null) {
                for (final ColumnDef refColumn : refTable.getValueColumns()) {
                    //FIXME deal with dimensions-with-dimensions
                    if (index++ > 1) sql.append(", ");
                    // qualified reference to the table:
                    sql.append(refTable.getTableName());
                    sql.append(".");
                    sql.append(refColumn.getColumnName());
                    // column name in the view:
                    sql.append(" ");
                    if (!refTable.getTableName().equalsIgnoreCase("PATH_DIM")) { //FIXME provide a nicer way to massage the column names
                        sql.append(refTable.getName());
                        sql.append("_");
                    }
                    sql.append(refColumn.getColumnName());
                }
            } else {
                if (index++ > 1) sql.append(", ");
                sql.append(table.getTableName());
                sql.append(".");
                sql.append(column.getColumnName());
            }
        }
        sql.append(" FROM ");
        sql.append(table.getTableName());
        for (final ColumnDef column : table.getValueColumns()) {
            final TableDef refTable = column.getReferencedTable();
            if (refTable != null) {
                sql.append(" JOIN ");
                sql.append(refTable.getTableName());
                sql.append(" ON ");
                sql.append(table.getTableName());
                sql.append(".");
                sql.append(column.getColumnName());
                sql.append(" = ");
                sql.append(refTable.getTableName());
                sql.append(".");
                sql.append(refTable.getSurrogateKeyColumnName());
            }
        }
        return sql.toString();
    }


    private void createTable(Statement stmt, TableDef table) throws SQLException {
        final String sql = buildCreateTableSql(table);
        logger.info("creating table " + table.getTableName() + ": " + sql);
        stmt.executeUpdate(sql);
    }

    private String buildCreateTableSql(final TableDef table) {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(table.getTableName());
        sb.append("(");
        int index = 1;
        if (table.hasSurrogateKey()) {
            sb.append(table.getSurrogateKeyColumnName());
            sb.append(" ");
            sb.append(SURROGATE_KEY_TYPE);
            sb.append(" AUTO_INCREMENT PRIMARY KEY HASH ");
            index++;
        }
        for (final ColumnDef column : table.getValueColumns()) {
            if (index++ > 1) sb.append(", ");
            sb.append(column.getColumnName());
            sb.append(" ");
            sb.append(getTypeForColumn(table, column));
            if (!column.isNullable()) {
                sb.append(" NOT NULL");
            }
        }
        sb.append(")");
        return sb.toString();
    }


    private void createIndex(final Statement stmt, final TableDef table, final String columnName, boolean hashIndex) throws SQLException {
        final String indexSql = buildCreateIndexSql(table, columnName, hashIndex);
        logger.info("creating index for " + table.getTableName() + "." + columnName + ": " + indexSql);
        stmt.executeUpdate(indexSql);
    }

    private String buildCreateIndexSql(final TableDef table, final String columnName, boolean hashIndex) {
        final StringBuilder sb = new StringBuilder();
        final String indexName = table.getTableName() + "__" + columnName + "__INDEX";
        sb.append("CREATE ");
        if (hashIndex) sb.append("HASH ");
        sb.append("INDEX " + indexName);
        sb.append(" ON ");
        sb.append(table.getTableName() + "(" + columnName + ")");
        return sb.toString();
    }


    private String getTypeForColumn(TableDef table, ColumnDef column) {
        switch (column.getType()) {
            case INTEGER:
                return "INTEGER";
            case LONG:
                return "BIGINT";
            case DOUBLE:
                return "DOUBLE";
            case BOOLEAN:
                return "BOOLEAN";
            case DATE:
            case TIMESTAMP:
                return "TIMESTAMP"; //??
            case STRING:
                return "VARCHAR(255)"; //FIXME
            case SURROGATE_KEY:
            case FOREIGN_KEY:
                return SURROGATE_KEY_TYPE;
            default:
                throw new IllegalArgumentException("invalid type " + column.getType());
        }
    }

    /**
     * A real connection pool seems pointless since every thread is going to be hammering on the db.
     */
    private static class ThreadLocalConnectionPool implements Closeable {

        private final ThreadLocal<Connection> threadLocal;
        private final Collection<Connection> allConnections;
        private final String connectionString;
        private final String username;
        private final String password;
        private final Logger logger;

        ThreadLocalConnectionPool(String connectionString, String username, String password, Logger logger) {
            this.connectionString = requireNonNull(connectionString);
            this.username = requireNonNull(username);
            this.password = requireNonNull(password);
            this.threadLocal = new ThreadLocal();
            this.allConnections = new ArrayList(16);
            this.logger = requireNonNull(logger);
        }

        public Connection get() throws SQLException {
            Connection out = threadLocal.get();
            if (out == null || out.isClosed()) {
                logger.info("opening db connection to " + connectionString);
                out = DriverManager.getConnection(connectionString, username, password);
                threadLocal.set(out);
                allConnections.add(out);
            }
            return out;
        }

        @Override
        public void close() throws IOException {
            for (Connection c : this.allConnections) {
                try {
                    c.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Problem closing db connection", e);
                }
            }
        }
    }
}
