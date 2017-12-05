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

import org.flywaydb.core.api.FlywayException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for PlaceholderReplacer.
 */
public class PlaceholderReplacerSmallTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test string to check functionality.
     */
    private static final String TEST_STR = "No ${placeholder} #[left] to ${replace}";

    @Test
    public void antStylePlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("placeholder", "value");
        placeholders.put("replace", "be replaced");
        placeholders.put("dummy", "shouldNotAppear");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        assertEquals("No value #[left] to be replaced", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void exoticPlaceholders() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("left", "right");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "#[", "]");

        assertEquals("No ${placeholder} right to ${replace}", placeholderReplacer.replacePlaceholders(TEST_STR));
    }

    @Test
    public void issue725RegexNPE() {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("database", "abc");
        placeholders.put("useLocal", "");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "$[", "]");

        String input = "INSERT INTO `source` VALUES (138, 'Source DN', 'Source DN', now(), NULL);\n" +
                "INSERT INTO `source_location` (`id`, `url`, `active`, `type`, `source_id`, `m_created`, `m_modified`) VALUES \n" +
                "(386, 'http://someUrl.si/feed/?cat=32,7,10,9,64&amp;feed=rss2&amp;tag__not_in=77', 1, 'news', 138, now(), NULL);";
        assertEquals(input, placeholderReplacer.replacePlaceholders(input));
    }

    @Test
    public void unmatchedPlaceholders() throws FlywayException {
        thrown.expect(FlywayException.class);
        thrown.expectMessage("No value provided for placeholder expressions: #[left].  Check your configuration!");
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "#[", "]");
        placeholderReplacer.replacePlaceholders(TEST_STR);
    }

    @Test
    public void unmatchedPlaceholdersWithMultipleOccurences() throws FlywayException {
        thrown.expect(FlywayException.class);
        thrown.expectMessage("No value provided for placeholder expressions: ${placeholder}, ${replace}.  Check your configuration!");
        Map<String, String> placeholders = new HashMap<String, String>();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");
        placeholderReplacer.replacePlaceholders(TEST_STR + TEST_STR);
    }

    @Test
    public void noPlaceholders() {
        PlaceholderReplacer placeholderReplacer = PlaceholderReplacer.NO_PLACEHOLDERS;
        assertEquals(TEST_STR, placeholderReplacer.replacePlaceholders(TEST_STR));
    }
}
