/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.schema;

import com.salesforce.ginsu.warehouse.SurrogateKey;

import java.util.List;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface TableBuilder {

    TableBuilder needsPrimaryKey(boolean b);

    TableBuilder needsSurrogateKey(boolean b);

    TableBuilder allowForeignKeys(boolean b);

    TableBuilder tableName(String tableName);

    <T> ColumnBuilder<T> addColumn(String name, ColumnType type);

    ColumnBuilder<SurrogateKey> addForeignKey(Table referencedTable);

    TableBuilder setPrimaryKeyColumns(List<Column> columns);

    TableBuilder setMaxRowsHint(long maxRows);

    Table build();
}
