/**
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.ginsu.persist.h2;

import com.salesforce.ginsu.persistence.PersistenceException;

import java.nio.file.Path;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author pcal
 * @since 0.0.1
 */
public class H2PersistenceProviderBuilder {

    private static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";
    private static final String DEFAULT_CONNECTION_STRING_FORMAT = "jdbc:h2:%s;CACHE_SIZE=131072";

    // During load, this doesn't help and actually hurts a little.  OS paging I assume.  Probably better off implementing
    // our own caching for upserts.  It helps when querying, though.
    // MAX_MEMORY_ROWS=10000000

    // This hurts query performance.  Just zipping the whole file is almost as good.
    // COMPRESS=TRUE

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASS = "";
    private static final int DEFAULT_INSERT_BATCH_SIZE = 50000;

    private Path dbFile;
    private String connectionString;
    private String jdbcDriver = DEFAULT_JDBC_DRIVER;
    private String username = DEFAULT_USER;
    private String password = DEFAULT_PASS;
    private int insertBatchSize = DEFAULT_INSERT_BATCH_SIZE;

    private Logger logger;

    public H2PersistenceProviderBuilder() {
    }

    public H2PersistenceProviderBuilder dbFile(final Path dbFile) {
        this.dbFile = requireNonNull(dbFile);
        return this;
    }

    public H2PersistenceProviderBuilder jdbcDriver(final String jdbcDriver) {
        this.jdbcDriver = requireNonNull(jdbcDriver);
        return this;
    }

    public H2PersistenceProviderBuilder connectionString(final String connectionString) {
        this.connectionString = requireNonNull(connectionString);
        return this;
    }

    public H2PersistenceProviderBuilder username(final String username) {
        this.username = requireNonNull(username);
        return this;
    }

    public H2PersistenceProviderBuilder password(final String password) {
        this.password = requireNonNull(password);
        return this;
    }

    public H2PersistenceProviderBuilder insertBatchSize(final int insertBatchSize) {
        this.insertBatchSize = insertBatchSize;
        return this;
    }

    public H2PersistenceProviderBuilder logger(final Logger logger) {
        this.logger = requireNonNull(logger);
        return this;
    }

    public H2PersistenceProvider build() throws PersistenceException {
        try {
            Class.forName(this.jdbcDriver);
        } catch (final ClassNotFoundException cnfe) {
            throw new PersistenceException(cnfe);
        }
        final String connectionStringToUse;
        {
            if (this.connectionString == null) {
                if (dbFile == null) {
                    throw new IllegalStateException("You must provide dbFile or connectionString");
                } else {
                    connectionStringToUse = String.format(DEFAULT_CONNECTION_STRING_FORMAT,
                            dbFile.toString());
                }
            } else {
                connectionStringToUse = this.connectionString;
            }
        }
        if (logger == null) logger = Logger.getAnonymousLogger();

        return new H2PersistenceProvider(connectionStringToUse, username, password, insertBatchSize,
                logger == null ? Logger.getAnonymousLogger() : logger);
    }


}
