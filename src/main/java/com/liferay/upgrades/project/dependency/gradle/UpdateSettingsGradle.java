package com.liferay.upgrades.project.dependency.gradle;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSettingsGradle {

    public String run(String directory, String newVersion) throws Exception {

        Path path = Paths.get(directory, "settings.gradle");

        if (!Files.exists(path)) {
            throw new FileNotFoundException("settings.gradle not found at: " + path.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(path);

        List<String> updatedLines = new ArrayList<>();

        String oldVersion = "legacy";

        boolean found = false;

        Pattern pattern = Pattern.compile("name:\\s*[\"']" + _pluginId + "[\"'].*version:\\s*[\"'](.*?)[\"']");

        for (String line: lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                oldVersion = matcher.group(1);

                String updatedLine = line.replace(oldVersion, newVersion);
                updatedLines.add(updatedLine);

                found = true;
                _log.info("Updated " + _pluginId + " from " + oldVersion + " to " + newVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            Files.write(path, updatedLines);
        } else {
            _log.warning("Could not find the workspace plugin definition in settings.gradle.");
        }

        return oldVersion;
    }

    private static final Logger _log = Logger.getLogger(UpdateSettingsGradle.class.getName());
    private static final String _pluginId = "com.liferay.gradle.plugins.workspace";
}
