/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;

import java.util.List;
import java.util.Map;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface TableDef {

    String getName();

    String getTableName();

    boolean hasSurrogateKey();

    String getSurrogateKeyColumnName();

    List<ColumnDef> getValueColumns();

}