package com.liferay.upgrades.project.dependency.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UpdateGradleWrapper {

    public String run(String directory, String gradleVersion) throws Exception {

        Path path = Paths.get(directory, "gradle", "wrapper", "gradle-wrapper.properties");

        if (!Files.exists(path)) {
            _log.warning("gradle-wrapper.properties not found at: " + path.toAbsolutePath() + ". Skipping");
            return null;
        }

        List<String> lines = Files.readAllLines(path);

        List<String> updatedLines = new ArrayList<>();

        String oldVersion = "unknown";
        boolean found = false;

        for (String line: lines) {

            String trimmedLine = line.trim();

            if (trimmedLine.startsWith(_distUrlKey)) {

                oldVersion = _extractVersion(trimmedLine);

                String newUrl = "https\\://services.gradle.org/distributions/gradle-" + gradleVersion + "-bin.zip";

                updatedLines.add(_distUrlKey + newUrl);
                found = true;
                _log.info("Updating Gradle Wrapper distributionUrl to version " + gradleVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            Files.write(path, updatedLines);
        }

        return oldVersion;
    }

    private String _extractVersion(String line) {
        try {
            int start = line.indexOf("gradle-") + 7;
            int end = line.indexOf("-", start);
            return line.substring(start, end);
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static final String _distUrlKey = "distributionUrl=";
    private static final Logger _log = Logger.getLogger(UpdateGradleWrapper.class.getName());
}
