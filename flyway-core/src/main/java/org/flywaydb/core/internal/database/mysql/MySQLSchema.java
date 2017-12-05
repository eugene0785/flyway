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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MySQL implementation of Schema.
 */
public class MySQLSchema extends Schema<MySQLDatabase> {
    /**
     * Creates a new MySQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    MySQLSchema(JdbcTemplate jdbcTemplate, MySQLDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select "
                        + "(Select count(*) from information_schema.TABLES Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.VIEWS Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.TABLE_CONSTRAINTS Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.EVENTS Where EVENT_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.TRIGGERS Where TRIGGER_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.ROUTINES Where ROUTINE_SCHEMA=?)",
                name, name, name, name, name, name
        );
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : cleanEvents()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanRoutines()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (Table table : allTables()) {
            table.drop();
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    /**
     * Generate the statements to clean the events in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanEvents() throws SQLException {
        List<String> eventNames =
                jdbcTemplate.queryForStringList(
                        "SELECT event_name FROM information_schema.events WHERE event_schema=?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (String eventName : eventNames) {
            statements.add("DROP EVENT " + database.quote(name, eventName));
        }
        return statements;
    }

    /**
     * Generate the statements to clean the routines in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanRoutines() throws SQLException {
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList(
                        "SELECT routine_name as 'N', routine_type as 'T' FROM information_schema.routines WHERE routine_schema=?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("N");
            String routineType = row.get("T");
            statements.add("DROP " + routineType + " " + database.quote(name, routineName));
        }
        return statements;
    }

    /**
     * Generate the statements to clean the views in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.views WHERE table_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW " + database.quote(name, viewName));
        }
        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'", name);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new MySQLTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new MySQLTable(jdbcTemplate, database, this, tableName);
    }
}
