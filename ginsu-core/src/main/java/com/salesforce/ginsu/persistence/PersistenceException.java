/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persistence;

/**
 * @author pcal
 * @since 0.0.1
 */
public class PersistenceException extends Exception {

    /**
     * @see Exception#Exception()
     */
    public PersistenceException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public PersistenceException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable, boolean, boolean)
     */
    protected PersistenceException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
