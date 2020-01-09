/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.schema;

import com.salesforce.ginsu.impl.SchemaBuilderImpl;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface SchemaBuilder {

    static SchemaBuilder create() {
        return new SchemaBuilderImpl();
    }

    TableBuilder dimensionTableBuilder(String name);

    TableBuilder junkDimensionTableBuilder(String name);

    TableBuilder factTableBuilder(String name);

    Schema build();

}
