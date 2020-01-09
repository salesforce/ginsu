/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence.mock;

import com.salesforce.ginsu.persistence.ColumnDef;
import com.salesforce.ginsu.persistence.Row;
import com.salesforce.ginsu.warehouse.SurrogateKey;
import com.salesforce.ginsu.persistence.TableDef;

import java.io.PrintWriter;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
class MockTable {

    private final TableDef tableDef;
    private long nextKey;
    private Map<Row, Row> rows = new LinkedHashMap<Row, Row>();

    MockTable(TableDef tableDef) {
        this.tableDef = requireNonNull(tableDef);
    }

    synchronized SurrogateKey insert(final Row row) {
        requireNonNull(row);
        if (row.getKey() != null) {
            throw new IllegalArgumentException("Row has a primary key; has it already been inserted?");
        }
        final SurrogateKey key = new MockSurrogateKey(nextKey++);
        row.setKey(key);
        rows.put(row, row);
        return key;
    }

    synchronized SurrogateKey upsert(final Row row) {
        final SurrogateKey existingRowKey = selectByValues(row);
        if (existingRowKey != null) {
            row.setKey(existingRowKey);
            rows.put(row, row);
            return existingRowKey;
        } else {
            return insert(row);
        }
    }

    void dump(PrintWriter out) {
        if (tableDef.hasSurrogateKey()) {
            out.print(tableDef.getSurrogateKeyColumnName());
            out.print(", ");
        }
        for (final ColumnDef col : tableDef.getValueColumns()) {
            out.print(col.getColumnName());
            out.print(", ");
        }
        out.println();
        for (final Row row : this.rows.values()) {
            if (tableDef.hasSurrogateKey()) {
                out.print(row.getKey());
                out.print(", ");
            }
            for (final ColumnDef col : tableDef.getValueColumns()) {
                out.print(row.getValue(col));
                out.print(", ");
            }
            out.println();
        }
    }

    private SurrogateKey selectByValues(final Row row) {
        final Row existingRow = rows.get(row);
        if (existingRow == null) {
            return null;
        } else {
            final SurrogateKey out = existingRow.getKey();
            if (out == null) {
                throw new IllegalStateException("null key in existingRow? " + existingRow);
            }
            return out;
        }
    }
}
