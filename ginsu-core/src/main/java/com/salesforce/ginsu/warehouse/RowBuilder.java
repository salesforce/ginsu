/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.warehouse;

import com.salesforce.ginsu.schema.Column;
import com.salesforce.ginsu.schema.Table;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface RowBuilder {

    <V> RowBuilder set(Column<V> columnId, V value);

    SurrogateKey insert();

    SurrogateKey upsert();

    /**
     * @return the table we're inserting to.
     */
    Table getTargetTable();

}
