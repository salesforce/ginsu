/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.salesforce.ginsu.persistence.ColumnDef;
import com.salesforce.ginsu.persistence.mock.MockPersistenceProvider;
import com.salesforce.ginsu.schema.*;
import com.salesforce.ginsu.warehouse.*;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

/**
 * @author pcal
 * @since 0.0.1
 */
public class RowImplTest extends TestCase {

    public void testBuildersCantBeReused() throws Exception {

        final SchemaBuilder sb = SchemaBuilder.create();
        final TableBuilder tb = sb.factTableBuilder("foo");
        final ColumnBuilder cb = sb.factTableBuilder("bar").addColumn("baz", ColumnType.STRING);
        cb.build();
        try {
            cb.build();
            fail("didn't get expected IllegalStateException when reusing ColumnBuilder");
        } catch (IllegalStateException expected) {
        }
        tb.build();
        try {
            tb.build();
            fail("didn't get expected IllegalStateException when reusing TableBuilder");
        } catch (IllegalStateException expected) {
        }

        final WarehouseBuilder wb = WarehouseBuilder.create().setSchema(sb.build()).setPersistence(new MockPersistenceProvider());
        final Warehouse w = wb.build();
        try {
            wb.build();
            fail("didn't get expected IllegalStateException when reusing WarehouseBuilder");
        } catch (IllegalStateException expected) {
        }
    }

    public void testRowEquality() throws Exception {

        final SchemaBuilder sb = SchemaBuilder.create();
        final TableBuilder tb = sb.factTableBuilder("foo");
        final Column col = tb.addColumn("bar", ColumnType.STRING).build();
        final TableImpl ti = (TableImpl) tb.build();

        final MockPersistenceProvider pp = new MockPersistenceProvider();
        final Warehouse w = WarehouseBuilder.create().setSchema(sb.build()).setPersistence(pp).build();


        final Map<ColumnDef, Object> valuesA = new HashMap();
        valuesA.put((ColumnDef) col, "hello!");
        final RowImpl rowA1 = new RowImpl(ti, valuesA);
        final RowImpl rowA2 = new RowImpl(ti, valuesA);
        final Map<ColumnDef, Object> valuesB1 = new HashMap();
        valuesB1.put((ColumnDef) col, "goodbye!");
        final RowImpl rowB1 = new RowImpl(ti, valuesB1);
        final Map<ColumnDef, Object> valuesB2 = new HashMap();
        valuesB2.put((ColumnDef) col, "goodbye!");
        final RowImpl rowB2 = new RowImpl(ti, valuesB2);

        assertEquals(rowA1, rowA2);
        assertEquals(rowA1.hashCode(), rowA2.hashCode());
        assertEquals(rowB1, rowB2);
        assertEquals(rowB1.hashCode(), rowB2.hashCode());
        assertNotEquals(rowA1, rowB1);
        assertNotEquals(rowA2, rowB2);
    }
}