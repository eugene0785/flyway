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
package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * An Ascii table.
 */
public class AsciiTable {
    private final List<String> columns;
    private final List<List<String>> rows;
    private final String nullText;
    private final String emptyText;

    /**
     * Creates a new Ascii table.
     *
     * @param columns   The column titles.
     * @param rows      The data rows
     * @param nullText  The text to use for a {@code null} value.
     * @param emptyText The text to include in the table if it has no rows.
     */
    public AsciiTable(List<String> columns, List<List<String>> rows, String nullText, String emptyText) {
        this.columns = columns;
        this.rows = rows;
        this.nullText = nullText;
        this.emptyText = emptyText;
    }

    /**
     * @return The table rendered with column header and row data.
     */
    public String render() {
        List<Integer> widths = new ArrayList<Integer>();
        for (String column : columns) {
            widths.add(column.length());
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                widths.set(i, Math.max(widths.get(i), getValue(row, i).length()));
            }
        }

        StringBuilder ruler = new StringBuilder("+");
        for (Integer width : widths) {
            ruler.append("-").append(StringUtils.trimOrPad("", width, '-')).append("-+");
        }
        ruler.append("\n");

        StringBuilder header = new StringBuilder("|");
        for (int i = 0; i < widths.size(); i++) {
            header.append(" ").append(StringUtils.trimOrPad(columns.get(i), widths.get(i), ' ')).append(" |");
        }
        header.append("\n");

        StringBuilder result = new StringBuilder();
        result.append(ruler);
        result.append(header);
        result.append(ruler);

        if (rows.isEmpty()) {
            result.append("| ").append(StringUtils.trimOrPad(emptyText, header.length() - 5)).append(" |\n");
        } else {
            for (List<String> row : rows) {
                StringBuilder r = new StringBuilder("|");
                for (int i = 0; i < widths.size(); i++) {
                    r.append(" ").append(StringUtils.trimOrPad(getValue(row, i), widths.get(i), ' ')).append(" |");
                }
                r.append("\n");
                result.append(r);
            }
        }

        result.append(ruler);
        return result.toString();
    }

    private String getValue(List<String> row, int i) {
        String value = row.get(i);
        if (value == null) {
            value = nullText;
        }
        return value;
    }
}
