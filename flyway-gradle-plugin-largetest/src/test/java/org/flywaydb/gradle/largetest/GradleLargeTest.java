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
package org.flywaydb.gradle.largetest;

import org.junit.Test;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Gradle Plugin.
 */
@SuppressWarnings({"JavaDoc"})
@RunWith(Parameterized.class)
public class GradleLargeTest {
    private final String installDir = System.getProperty("installDir", "flyway-gradle-plugin-largetest/target/test-classes");
    private final String version;

    public GradleLargeTest(String version) {
        this.version = version;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"30"},
                {"40"}});
    }

    @Test(timeout = 60000)
    public void regular() throws Exception {
        String stdOut = runGradle(0, "regular", "clean", "flywayMigrate", "-Pflyway.placeholders.name=James");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    @Test(timeout = 60000)
    public void jdbcDriverAsModuleDependency() throws Exception {
        String stdOut = runGradle(0, "jdbcdriver-dependency", "clean", "flywayMigrate", "-Pflyway.placeholders.name=James");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    @Test(timeout = 60000)
    public void custom() throws Exception {
        String stdOut = runGradle(0, "custom", "clean", "someTestMigration", "-Pflyway.placeholders.name=James");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    @Test(timeout = 60000)
    public void error() throws Exception {
        String stdOut = runGradle(0, "error", "clean", "flywayMigrate");
        assertTrue(stdOut.contains("Successfully validated 0 migrations"));
    }

    /**
     * Runs Gradle in this directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param dir                The directory below src/test/resources to run Gradle in.
     * @param extraArgs          The extra arguments (if any) for Gradle.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    @SuppressWarnings("SameParameterValue")
    private String runGradle(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        String flywayVersion = System.getProperty("flywayVersion", getPomVersion());

        String extension = "";
        if (isWindowsOs()) {
            extension = ".bat";
        }

        List<String> args = new ArrayList<String>();
        addShellIfNeeded(args);
        args.add(installDir + "/install/" + version + "/gradlew" + extension);
        args.add("-PflywayVersion=" + flywayVersion);
        //args.add("--debug");
        args.add("--stacktrace");
        args.add("--no-daemon");
        args.add("-i");
        args.add("-b");
        args.add(installDir + "/tests/" + dir + "/build.gradle");
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(installDir + "/tests/" + dir));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        System.out.print(stdOut);
        int returnCode = process.waitFor();

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }

    private void addShellIfNeeded(List<String> args) {
        if (!isWindowsOs()) {
            args.add("bash");
        }
    }

    /**
     * Check if we are running on Windows OS
     *
     * @return true for windows, otherwise false
     */
    private boolean isWindowsOs() {
        return System.getProperty("os.name", "generic").toLowerCase().startsWith("windows");
    }

    /**
     * Retrieves the version embedded in the project pom. Useful for running these tests in IntelliJ.
     *
     * @return The POM version.
     */
    private String getPomVersion() {
        try {
            File pom = new File("pom.xml");
            if (!pom.exists()) {
                return "unknown";
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(pom);
            return xPath.evaluate("/project/version", document);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read POM version", e);
        }
    }
}
