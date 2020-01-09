/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;
import com.salesforce.ginsu.schema.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.salesforce.ginsu.schema.ColumnType.FOREIGN_KEY;
import static com.salesforce.ginsu.schema.ColumnType.SURROGATE_KEY;
import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
class TableBuilderImpl implements TableBuilder, Consumer<ColumnImpl> {

    private final Function<String, String> idColumnNamer;
    private final Set<String> usedColumnNames = new HashSet();
    private final String name;
    private final List<ColumnImpl> valueColumns;
    private List<ColumnImpl> primaryKeyColumns;
    private boolean hasSurrogateKey = true;
    private boolean hasPrimaryKey = true;
    private boolean allowForeignKeys = true;
    private String skColumnName;
    private TableImpl builtTable;
    private long maxRowsHint = Integer.MAX_VALUE;
    private String tableName;

    TableBuilderImpl(final String name, final Function<String, String> idColumnNamer) {
        this.name = requireNonNull(name);
        this.tableName = this.name;
        this.idColumnNamer = requireNonNull(idColumnNamer);
        this.valueColumns = new ArrayList<ColumnImpl>();
    }

    @Override
    public ColumnBuilder addColumn(final String name, final ColumnType type) {
        assertNotBuilt();
        if (type == FOREIGN_KEY || type == SURROGATE_KEY) {
            throw new IllegalArgumentException("cannot add " + type + " valueColumns");
        }
        return new ColumnBuilderImpl(name, type, this);
    }

    @Override
    public ColumnBuilder addForeignKey(Table referencedTable) {
        assertNotBuilt();
        requireNonNull(referencedTable);
        if (!allowForeignKeys) throw new IllegalStateException("foreign keys not allowed on this table");
        if (referencedTable instanceof TableImpl) {
            if (!((TableImpl) referencedTable).hasSurrogateKey()) {
                throw new IllegalArgumentException(referencedTable.getName() + " does not have a surrogate key");
            }
            return new ColumnBuilderImpl(this.idColumnNamer.apply(referencedTable.getName()), (TableImpl) referencedTable, this);
        } else {
            throw new IllegalArgumentException("unexpected " + referencedTable.getClass().getName());
        }
    }

    @Override
    public TableBuilder needsSurrogateKey(boolean hasSurrogateKey) {
        this.hasSurrogateKey = hasSurrogateKey;
        return this;
    }

    @Override
    public TableBuilder needsPrimaryKey(boolean b) {
        this.hasPrimaryKey = b;
        if (!this.hasPrimaryKey) this.primaryKeyColumns = null;
        return this;
    }

    @Override
    public TableBuilder setPrimaryKeyColumns(List<Column> columns) {
        this.hasPrimaryKey = true;
        this.primaryKeyColumns = CastUtils.immutableCopyCast(columns, ColumnImpl.class);
        return this;
    }

    @Override
    public TableBuilder allowForeignKeys(boolean b) {
        this.allowForeignKeys = b;
        return this;
    }

    @Override
    public TableBuilder tableName(String tableName) {
        this.tableName = requireNonNull(tableName);
        return this;
    }

    @Override
    public TableBuilder setMaxRowsHint(long maxRows) {
        maxRowsHint = maxRows;
        return this;
    }

    @Override
    public Table build() {
        assertNotBuilt();
        if (this.hasSurrogateKey) {
            if (this.skColumnName == null) {
                this.skColumnName = this.idColumnNamer.apply(this.name);
            }
            this.checkColumnName(this.skColumnName);
        } else {
            skColumnName = null;
        }
        return this.builtTable = new TableImpl(name, tableName, skColumnName, valueColumns);
    }

    @Override
    public void accept(ColumnImpl column) {
        checkColumnName(column.getColumnName());
        this.valueColumns.add(column);
    }

    TableImpl getBuiltTable() {
        return this.builtTable;
    }

    private void checkColumnName(String newColumnName) {
        if (usedColumnNames.contains(newColumnName)) {
            throw new IllegalArgumentException("duplicate column name: " + newColumnName);
        } else {
            usedColumnNames.add(newColumnName);
        }
    }

    private void assertNotBuilt() {
        if (this.builtTable != null) {
            throw new IllegalStateException("TableBuilders cannot be reused");
        }
    }
}
