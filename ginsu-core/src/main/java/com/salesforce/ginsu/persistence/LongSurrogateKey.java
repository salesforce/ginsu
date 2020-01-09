/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;

import com.salesforce.ginsu.warehouse.SurrogateKey;

import javax.annotation.concurrent.Immutable;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
@Immutable
public final class LongSurrogateKey implements SurrogateKey {

    private final long keyValue;

    public LongSurrogateKey(long keyValue) {
        this.keyValue = requireNonNull(keyValue);
    }

    public long getKeyValue() {
        return keyValue;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof LongSurrogateKey) {
            return this.keyValue == (((LongSurrogateKey) that).keyValue);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(keyValue);
    }

    @Override
    public String toString() {
        return String.valueOf(keyValue);
    }
}
