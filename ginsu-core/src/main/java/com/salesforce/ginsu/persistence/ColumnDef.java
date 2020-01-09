/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;

import com.salesforce.ginsu.schema.ColumnType;

import java.util.Map;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface ColumnDef {

    String getColumnName();

    ColumnType getType();

    /**
     * @return if this column is a FOREIGN_KEY, the TableDef for the referenced buildTable; otherwise returns null.
     */
    TableDef getReferencedTable();

    boolean isNullable();

    boolean needsIndex();
}
