/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.schema;

import com.salesforce.ginsu.warehouse.RowBuilder;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface Table {

    String getName();

    /**
     * @return true if the given column belongs to this table.
     */
    boolean contains(Column column);

}
