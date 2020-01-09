/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;
import com.salesforce.ginsu.schema.Schema;
import com.salesforce.ginsu.schema.Table;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;


/**
 * @author pcal
 * @since 0.0.1
 */
class SchemaImpl implements Schema {

    private final List<TableImpl> tables;

    public SchemaImpl(List<TableImpl> tables) {
        this.tables = requireNonNull(tables);
    }

    @Override
    public Collection<Table> getTables() {
        return ImmutableList.copyOf(tables);
    }
}
