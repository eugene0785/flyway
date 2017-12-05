/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.util.Properties;

import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_PASSWORD;
import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_PORT;
import static org.flywaydb.core.internal.database.sqlserver.SQLServerMigrationMediumTest.JDBC_USER;

/**
 * Test to demonstrate the migration functionality using SQL Server with the Microsoft driver.
 */
@Category(DbCategory.SQLServer.class)
public class MsSQLServerCaseSensitiveMigrationMediumTest extends SQLServerCaseSensitiveMigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                "jdbc:sqlserver://localhost:" + JDBC_PORT + ";databaseName=flyway_db_ms_case_sensitive", JDBC_USER, JDBC_PASSWORD);
    }
}
