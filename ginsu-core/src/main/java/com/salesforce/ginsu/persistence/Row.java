/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;

import com.salesforce.ginsu.warehouse.SurrogateKey;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface Row {

    SurrogateKey getKey();

    Object getValue(ColumnDef column);


    void setKey(SurrogateKey key);
}
