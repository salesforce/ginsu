/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;
import com.salesforce.ginsu.persistence.ColumnDef;
import com.salesforce.ginsu.persistence.TableDef;
import com.salesforce.ginsu.schema.Column;
import com.salesforce.ginsu.schema.Table;

import javax.annotation.concurrent.Immutable;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
@Immutable
final class TableImpl implements TableDef, Table {

    private final String name;
    private final String tableName;
    private final String skColumnNameOrNull;
    private final List<ColumnDef> valueColumns;

    public TableImpl(final String name, final String tableName, final String skColumnNameOrNull, final List<ColumnImpl> valueColumns) {
        this.name = requireNonNull(name);
        this.tableName = requireNonNull(tableName);
        this.skColumnNameOrNull = skColumnNameOrNull;
        this.valueColumns = ImmutableList.copyOf(requireNonNull(valueColumns));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean contains(Column column) {
        return valueColumns.contains(column);
    }

    @Override
    public List<ColumnDef> getValueColumns() {
        return this.valueColumns;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public boolean hasSurrogateKey() {
        return skColumnNameOrNull != null;
    }

    @Override
    public String getSurrogateKeyColumnName() {
        return skColumnNameOrNull;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof TableImpl)) return false;
        return this.name.equals(((TableImpl) that).name);
    }
}
