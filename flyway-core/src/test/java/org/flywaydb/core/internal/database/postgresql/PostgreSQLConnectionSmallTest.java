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
package org.flywaydb.core.internal.database.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgreSQLConnectionSmallTest {
    @Test
    public void getFirstSchemaFromSearchPath() {
        assertEquals("ABC", PostgreSQLConnection.getFirstSchemaFromSearchPath("\"ABC\", def"));
    }
}
