/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;
import com.salesforce.ginsu.schema.Schema;
import com.salesforce.ginsu.schema.SchemaBuilder;
import com.salesforce.ginsu.schema.TableBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public class SchemaBuilderImpl implements SchemaBuilder {

    private final Logger logger;
    private Function<String, String> idColumnNamer = IdColumnNamers.DEFAULT;
    private final List<TableBuilderImpl> tableBuilders = new ArrayList();

    public SchemaBuilderImpl() {
        this(Logger.getAnonymousLogger());
    }

    public SchemaBuilderImpl(Logger logger) {
        this.logger = requireNonNull(logger);
    }

    @Override
    public TableBuilder dimensionTableBuilder(String name) {
        final TableBuilderImpl out = new TableBuilderImpl(name, this.idColumnNamer);
        out.needsSurrogateKey(true).needsPrimaryKey(true).allowForeignKeys(false);
        out.tableName(name + "_DIM");
        tableBuilders.add(out);
        return out;
    }

    @Override
    public TableBuilder junkDimensionTableBuilder(String name) {
        // currently there's no difference; we should add some constraints on value types.  in the meantime, it
        // at least fosters clarity.
        return dimensionTableBuilder(name);
    }

    @Override
    public TableBuilder factTableBuilder(String name) {
        final TableBuilderImpl out = new TableBuilderImpl(name, this.idColumnNamer);
        out.needsSurrogateKey(false).needsPrimaryKey(false).allowForeignKeys(true);
        out.tableName(name + "_FACT");
        tableBuilders.add(out);
        return out;
    }

    @Override
    public Schema build() {
        final Set<String> usedTableNames = new HashSet<String>();
        final List<TableImpl> tables = new ArrayList<TableImpl>();
        for (final TableBuilderImpl tb : this.tableBuilders) {
            final TableImpl built = tb.getBuiltTable();
            if (built == null) {
                logger.warning("table was not built, ignoring " + tb);
            } else {
                tables.add(built);
                if (usedTableNames.contains(built.getTableName())) {
                    throw new IllegalStateException("duplicate table name " + built.getTableName());
                } else {
                    usedTableNames.add(built.getTableName());
                }
                final String viewName = built.getName(); //FIXME make a ViewBuilder
                if (usedTableNames.contains(viewName)) {
                    throw new IllegalStateException("duplicate view name " + viewName);
                } else {
                    usedTableNames.add(viewName);
                }
            }
        }
        return new SchemaImpl(ImmutableList.copyOf(tables));
    }

    private enum IdColumnNamers implements Function<String, String> {
        DEFAULT {
            @Override
            public String apply(String s) {
                return s + "_Id";
            }
        }
    }
}
