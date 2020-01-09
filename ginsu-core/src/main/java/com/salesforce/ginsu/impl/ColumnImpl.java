/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.persistence.ColumnDef;
import com.salesforce.ginsu.schema.Column;
import com.salesforce.ginsu.schema.ColumnType;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
@Immutable
final class ColumnImpl<T> implements Column<T>, ColumnDef {

    private final String name;
    private final T defaultValueOrNull;
    private final boolean isNullable;
    private final boolean needsIndex;
    private final ColumnType type;
    private final TableImpl referencedTable;
    private final Function<Object, T> valueConverter;

    ColumnImpl(String name, T defaultValueOrNull, boolean isNullable, ColumnType type, TableImpl referencedTable, Function<Object, T> valueConverterOrNull, boolean needsIndex) {
        this.name = requireNonNull(name);
        this.defaultValueOrNull = defaultValueOrNull;
        this.isNullable = isNullable;
        this.type = requireNonNull(type);
        this.valueConverter = valueConverterOrNull;
        this.needsIndex = needsIndex;
        switch (type) {
            case FOREIGN_KEY:
                this.referencedTable = requireNonNull(referencedTable);
                break;
            default:
                if (referencedTable != null) {
                    throw new IllegalArgumentException("referencedTable must be for with " + type);
                }
                this.referencedTable = null;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getColumnName() {
        return this.name;
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public TableImpl getReferencedTable() {
        return referencedTable;
    }

    @Override
    public boolean isNullable() {
        return this.isNullable;
    }

    @Override
    public boolean needsIndex() {
        return this.needsIndex;
    }

    public T getDefaultValue() {
        return this.defaultValueOrNull;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof ColumnImpl)) return false;
        return this.name.equals(((ColumnImpl) that).name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Asserts that the given value object can be assigned to this column.
     */
    Object prepareValue(Object value) {
        if (valueConverter != null) {
            value = valueConverter.apply(value);
        }
        if (value == null) {
            if (!isNullable()) {
                throw new IllegalArgumentException(this.name + " can't be set to null");
            }
        } else {
            if (!this.type.getJavaType().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException(this.name + " doesn't accept values of type " + value.getClass().getName());
            }
        }
        return value;
    }
}
