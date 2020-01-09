/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence.mock;

import com.salesforce.ginsu.persistence.*;
import com.salesforce.ginsu.warehouse.SurrogateKey;
import com.salesforce.ginsu.schema.Table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public class MockPersistenceProvider implements PersistenceProvider {

    private final Map<TableDef, MockTable> mockTables = new HashMap();

    @Override
    public SurrogateKey insert(final InsertMode mode, final TableDef table, final Row row) {
        switch (mode) {
            case INSERT:
                return findOrCreateTable(table).insert(row);
            case INSERT_ANONYMOUS:
                findOrCreateTable(table).insert(row);
                return null;
            case UPSERT:
                return findOrCreateTable(table).upsert(row);
            case UPSERT_ANONYMOUS:
                findOrCreateTable(table).upsert(row);
                return null;
            default:
                throw new IllegalStateException("unknown mode " + mode);
        }
    }

    @Override
    public void validateSchema(ValidateSchemaRequest request) {
    }

    @Override
    public void createSchema(CreateSchemaRequest request) {
    }

    @Override
    public void close() throws IOException {
    }

    public void dumpAll(final PrintWriter out) {
        for (final TableDef tableDef : this.mockTables.keySet()) {
            final MockTable table = requireNonNull(this.mockTables.get(tableDef));
            out.println("---------------------------");
            out.println(tableDef.getTableName());
            out.println("---------------------------");
            table.dump(out);
            out.println();
        }
    }

    public void dumpTable(Table table, final PrintWriter out) {
        for (final TableDef tableDef : this.mockTables.keySet()) {
            if (table.getName().equals(tableDef.getTableName())) {
                final MockTable mockTable = requireNonNull(this.mockTables.get(tableDef));
                out.println("---------------------------");
                out.println(tableDef.getTableName());
                out.println("---------------------------");
                mockTable.dump(out);
                out.println();
            }
        }
    }

    private synchronized MockTable findOrCreateTable(TableDef tableDef) {
        if (mockTables.containsKey(tableDef)) {
            return mockTables.get(tableDef);
        } else {
            final MockTable mockTable = new MockTable(tableDef);
            mockTables.put(tableDef, mockTable);
            return mockTable;
        }
    }

}
