/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.schema;

import com.salesforce.ginsu.warehouse.SurrogateKey;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public enum ColumnType {

    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    BOOLEAN(Boolean.class),
    DATE(Date.class),
    TIMESTAMP(Timestamp.class),
    STRING(String.class),
    FOREIGN_KEY(SurrogateKey.class),
    SURROGATE_KEY(SurrogateKey.class);

    private final Class<?> javaType;

    private ColumnType(Class<?> javaType) {
        this.javaType = requireNonNull(javaType);
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }
}
