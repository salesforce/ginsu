/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.schema;

import javax.annotation.concurrent.Immutable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
@Immutable
public final class EnumToStringValueConverter<T extends Enum> implements Function<Object, Object> {


    public static <T extends Enum> EnumToStringValueConverter create(Class<T> clazz) {
        return new EnumToStringValueConverter(clazz);
    }

    private final Class<T> clazz;

    private EnumToStringValueConverter(Class<T> clazz) {
        this.clazz = requireNonNull(clazz);
    }

    @Override
    public Object apply(final Object o) {
        if (o == null) {
            return null;
        } else if (this.clazz.isAssignableFrom(o.getClass())) {
            return ((Enum) o).name();
        } else {
            throw new IllegalArgumentException("invalid value object " + o.getClass());
        }
    }
}
