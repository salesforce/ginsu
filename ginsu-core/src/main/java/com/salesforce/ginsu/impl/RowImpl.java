/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.persistence.ColumnDef;
import com.salesforce.ginsu.persistence.Row;
import com.salesforce.ginsu.warehouse.SurrogateKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static java.util.Objects.requireNonNull;


/**
 * @author pcal
 * @since 0.0.1
 */
class RowImpl implements Row {

    private final Map<? extends ColumnDef, Object> values;
    private final TableImpl table;
    private SurrogateKey key;
    private transient Integer hashCode;

    RowImpl(TableImpl table, Map<? extends ColumnDef, Object> values) {
        this.table = requireNonNull(table);
        this.values = requireNonNull(values);
    }

    @Override
    public Object getValue(ColumnDef column) {
        return values.get(column);
    }

    @Override
    public SurrogateKey getKey() {
        return this.key;
    }

    @Override
    public void setKey(final SurrogateKey key) {
        if (this.key != null) {
            throw new IllegalStateException("key cannot be changed");
        }
        this.key = requireNonNull(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof RowImpl)) {
            return false;
        }
        final RowImpl that = (RowImpl) o;
        if (!this.table.equals(that.table)) return false;
        for (final ColumnDef col : this.table.getValueColumns()) {
            final Object ours = this.getValue(col);
            final Object theirs = that.getValue(col);
            if (!(ours == null ? theirs == null : ours.equals(theirs))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (this.hashCode != null) {
            return this.hashCode;
        } else {
            final HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(table);
            for (final ColumnDef col : this.table.getValueColumns()) {
                final Object value = this.getValue(col);
                hcb.append(value);
            }
            return this.hashCode = hcb.toHashCode();
        }
    }
}
