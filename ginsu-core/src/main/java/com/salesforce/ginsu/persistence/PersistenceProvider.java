/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;


import com.salesforce.ginsu.warehouse.SurrogateKey;

import java.io.Closeable;
import java.util.List;

/**
 * @author pcal
 * @since 0.0.1
 */
public interface PersistenceProvider extends Closeable {

    interface ValidateSchemaRequest {
        List<TableDef> getTables();
    }

    void validateSchema(ValidateSchemaRequest request) throws PersistenceException;

    interface CreateSchemaRequest {
        List<TableDef> getTables();
    }

    void createSchema(CreateSchemaRequest request) throws PersistenceException;

    enum InsertMode {
        INSERT,
        INSERT_ANONYMOUS,
        UPSERT,
        UPSERT_ANONYMOUS
    }

    SurrogateKey insert(InsertMode mode, TableDef table, Row values); // killme




    interface UpdateRequest {

        enum Mode {
            INSERT,
            UPSERT,
            MERGE,
        }

        Mode getMode();

        TableDef getTableDef();

        Row getRow();

        boolean isReturnValueRequired();

    }

    default SurrogateKey update(UpdateRequest request) {
        return null;
    }

}
