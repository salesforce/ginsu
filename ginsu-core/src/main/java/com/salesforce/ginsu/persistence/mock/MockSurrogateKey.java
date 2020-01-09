/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence.mock;

import com.salesforce.ginsu.warehouse.SurrogateKey;

import javax.annotation.concurrent.Immutable;

/**
 * @author pcal
 * @since 0.0.1
 */
@Immutable
class MockSurrogateKey implements SurrogateKey {

    private final long keyValue;

    MockSurrogateKey(long value) {
        this.keyValue = value;
    }

    @Override
    public String toString() {
        return String.valueOf(keyValue);
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof MockSurrogateKey) {
            return this.keyValue == ((MockSurrogateKey) that).keyValue;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(keyValue);
    }
}
