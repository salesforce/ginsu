/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.impl;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * @author pcal
 * @since 0.0.1
 */
class CastUtils {

    static <T> T castExactly(Object thing, Class<T> as) {
        if (thing.getClass().equals(as)) {
            return (T) thing;
        } else {
            throw new IllegalArgumentException("expected an instance of " + as.getName() + " but was " + thing.getClass().getName());
        }
    }

    static <T> T cast(Object thing, Class<T> as) {
        if (as.isAssignableFrom(thing.getClass())) {
            return (T) thing;
        } else {
            throw new IllegalArgumentException("expected an instance of " + as.getName() + " but was " + thing.getClass().getName());
        }
    }

    static <T> List<T> immutableCopyCastExactly(Collection<?> things, Class<T> as) {
        final ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (Object thing : things) {
            builder.add(castExactly(thing, as));
        }
        return builder.build();
    }

    static <T> List<T> immutableCopyCast(Collection<?> things, Class<T> as) {
        final ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (Object thing : things) {
            builder.add(cast(thing, as));
        }
        return builder.build();
    }


}
