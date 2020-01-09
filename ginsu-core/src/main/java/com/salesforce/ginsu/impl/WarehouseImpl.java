/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;
import com.salesforce.ginsu.persistence.PersistenceProvider;

import static com.salesforce.ginsu.persistence.PersistenceProvider.*;

import com.salesforce.ginsu.persistence.PersistenceException;
import com.salesforce.ginsu.persistence.TableDef;
import com.salesforce.ginsu.schema.Schema;
import com.salesforce.ginsu.schema.Table;

import static java.util.Objects.requireNonNull;

import com.salesforce.ginsu.warehouse.RowBuilder;
import com.salesforce.ginsu.warehouse.Warehouse;

import java.io.IOException;
import java.util.List;


/**
 * @author pcal
 * @since 0.0.1
 */
class WarehouseImpl implements Warehouse {

    private final Schema schema;
    private final PersistenceProvider pp;

    WarehouseImpl(final Schema schema, final PersistenceProvider persistence) throws PersistenceException {
        this.schema = requireNonNull(schema);
        this.pp = requireNonNull(persistence);
        final List<TableDef> tables = CastUtils.immutableCopyCast(schema.getTables(), TableDef.class);
        //FIXME need to do a topo sort here
        this.pp.validateSchema(new ValidateSchemaRequest() {
            @Override
            public List<TableDef> getTables() {
                return tables;
            }
        });
        this.pp.createSchema(new CreateSchemaRequest() {
            @Override
            public List<TableDef> getTables() {
                return tables;
            }
        });
    }

    @Override
    public RowBuilder buildRow(Table table) {
        requireNonNull(table);
        if (!(table instanceof TableImpl)) {
            throw new IllegalArgumentException("invalid impl " + table.getClass());
        }
        return new RowBuilderImpl(((TableImpl) table), this.pp);
    }

    @Override
    public void close() throws IOException {
        pp.close();
    }
}
