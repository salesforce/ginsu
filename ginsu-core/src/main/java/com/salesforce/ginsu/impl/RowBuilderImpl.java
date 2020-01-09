/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.persistence.ColumnDef;

import com.salesforce.ginsu.persistence.PersistenceProvider;
import com.salesforce.ginsu.persistence.PersistenceProvider.InsertMode;

import static com.salesforce.ginsu.persistence.PersistenceProvider.InsertMode.*;

import com.salesforce.ginsu.persistence.Row;
import com.salesforce.ginsu.schema.Column;
import com.salesforce.ginsu.schema.Table;
import com.salesforce.ginsu.warehouse.SurrogateKey;
import com.salesforce.ginsu.warehouse.RowBuilder;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
class RowBuilderImpl implements RowBuilder {

    private final PersistenceProvider pp;
    private final TableImpl table;
    private Map<ColumnImpl<?>, Object> values;

    RowBuilderImpl(TableImpl tableDef, PersistenceProvider pp) {
        this.table = requireNonNull(tableDef);
        this.pp = requireNonNull(pp);
        this.values = new HashMap();
    }

    @Override
    public RowBuilder set(Column column, Object value) {
        requireNonNull(column);
        if (!table.contains(column)) {
            throw new IllegalArgumentException(column.getName() + " does not belong to table " + table.getName());
        }
        checkState();
        if (!(column instanceof ColumnImpl)) {
            throw new IllegalArgumentException("invalid Column implementation " + column.getClass().getName());
        }
        final ColumnImpl columnImpl = (ColumnImpl) column;
        if (value == null && !columnImpl.isNullable()) {
            throw new IllegalArgumentException(((ColumnImpl) column).getColumnName() + " is not nullable");
        }
        value = columnImpl.prepareValue(value);
        this.values.put(columnImpl, value);
        return this;
    }

    @Override
    public SurrogateKey insert() {
        checkState();
        return executeUpdate(this.table.hasSurrogateKey() ? INSERT : INSERT_ANONYMOUS);
    }

    @Override
    public SurrogateKey upsert() {
        checkState();
        return executeUpdate(this.table.hasSurrogateKey() ? UPSERT : UPSERT_ANONYMOUS);
    }

    @Override
    public Table getTargetTable() {
        return this.table;
    }

    private SurrogateKey executeUpdate(final InsertMode mode) {
        checkState();
        final Row row = new RowImpl(this.table, fillInDefaults(this.table, this.values));
        final SurrogateKey key = pp.insert(mode, this.table, row);
        switch (mode) {
            case INSERT:
            case UPSERT:
                return key;
            case INSERT_ANONYMOUS:
            case UPSERT_ANONYMOUS:
                if (key != null) {
                    throw new IllegalStateException("unexpected non-null key on " + mode);
                }
                return key;
            default:
                throw new IllegalStateException("invalid mode " + mode);
        }
    }

    private Map<? extends ColumnDef, Object> fillInDefaults(final TableImpl table, final Map<ColumnImpl<?>, Object> values) {
        for (final ColumnDef cd : table.getValueColumns()) {
            if (!values.containsKey(cd) && !cd.isNullable()) {
                final ColumnImpl column = CastUtils.cast(cd, ColumnImpl.class);
                if (column.getDefaultValue() == null) {
                    throw new IllegalStateException(column.getName() + " is not nullable and has not default value; a value must be provided.");
                } else {
                    values.put(column, column.getDefaultValue());
                }
            }
        }
        return values;
    }

    private void checkState() {
        if (this.values == null) {
            throw new IllegalStateException("RowBuilders can't be reused.");
        }
    }
}
