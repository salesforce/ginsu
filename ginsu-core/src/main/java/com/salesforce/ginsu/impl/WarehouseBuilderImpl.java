/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.persistence.PersistenceException;
import com.salesforce.ginsu.persistence.PersistenceProvider;
import com.salesforce.ginsu.schema.Schema;
import com.salesforce.ginsu.warehouse.*;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public class WarehouseBuilderImpl implements WarehouseBuilder {

    private PersistenceProvider pp;
    private Schema schema;
    private WarehouseImpl built = null;

    public WarehouseBuilderImpl() {
    }

    @Override
    public WarehouseBuilderImpl setPersistence(PersistenceProvider persistence) {
        assertUsable();
        this.pp = requireNonNull(persistence);
        return this;
    }

    @Override
    public WarehouseBuilderImpl setSchema(Schema schema) {
        assertUsable();
        this.schema = requireNonNull(schema);
        return this;
    }

    @Override
    public Warehouse build() throws PersistenceException {
        assertUsable();
        if (this.pp == null) throw new IllegalStateException(PersistenceProvider.class.getSimpleName() + " not set");
        if (this.schema == null) throw new IllegalStateException(Schema.class.getSimpleName() + " not set");
        return built = new WarehouseImpl(this.schema, this.pp);
    }

    private void assertUsable() {
        if (built != null) throw new IllegalStateException("WarehouseBuilderImpl cannot be reused");
    }

}
