/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.schema.Column;
import com.salesforce.ginsu.schema.ColumnBuilder;
import com.salesforce.ginsu.schema.ColumnType;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
class ColumnBuilderImpl<T> implements ColumnBuilder<T> {

    private final Consumer<ColumnImpl> builtColumnConsumer;
    private final ColumnType type;
    private String name;
    private Properties properties;
    private Function<Object, T> valueConverter;
    private boolean isNullable = false;
    private final TableImpl referencedTable;
    private boolean isUsedUp = false;
    private boolean needsIndex = true;
    private T defaultValueOrNull;

    ColumnBuilderImpl(final String name, final ColumnType type, final Consumer<ColumnImpl> consumer) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
        this.referencedTable = null;
        this.builtColumnConsumer = requireNonNull(consumer);
    }

    ColumnBuilderImpl(final String name, final TableImpl referencedTable, final Consumer<ColumnImpl> consumer) {
        this.name = requireNonNull(name);
        this.type = ColumnType.FOREIGN_KEY;
        this.referencedTable = requireNonNull(referencedTable);
        this.builtColumnConsumer = requireNonNull(consumer);
    }

    @Override
    public ColumnBuilder nullable(boolean isNullable) {
        checkState();
        this.isNullable = isNullable;
        return this;
    }

    @Override
    public ColumnBuilder setMaxLength(int length) {
        checkState();
        return this;
    }

    @Override
    public ColumnBuilder valueConverter(Function<Object, T> valueConverter) {
        checkState();
        this.valueConverter = requireNonNull(valueConverter);
        return this;
    }

    @Override
    public ColumnBuilder needsIndex(boolean needsIndex) {
        this.needsIndex = needsIndex;
        return this;
    }

    @Override
    public ColumnBuilder<T> defaultValue(T v) {
        this.defaultValueOrNull = requireNonNull(v);
        return this;
    }

    @Override
    public Column<T> build() {
        checkState();
        final ColumnImpl<T> built = new ColumnImpl<T>(name, defaultValueOrNull, isNullable, type, referencedTable, valueConverter, needsIndex);
        this.builtColumnConsumer.accept(built);
        isUsedUp = true;
        return built;
    }

    private void checkState() {
        if (isUsedUp) throw new IllegalStateException("ColumnBuilder cannot be reused");
    }
}
