/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.warehouse;

import com.salesforce.ginsu.persistence.PersistenceProvider;
import com.salesforce.ginsu.impl.WarehouseBuilderImpl;
import com.salesforce.ginsu.persistence.PersistenceException;
import com.salesforce.ginsu.schema.Schema;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface WarehouseBuilder {

    static WarehouseBuilder create() {
        return new WarehouseBuilderImpl();
    }

    WarehouseBuilder setPersistence(PersistenceProvider persistence);

    WarehouseBuilder setSchema(Schema schema);

    Warehouse build() throws PersistenceException;
}
