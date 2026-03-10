package com.liferay.upgrades.project.dependency.sourceformatter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

public class SourceFormatterConfigurator {

    public void configureSourceFormatter(String directory, String targetRelease) throws Exception {
        File file = new File(directory, "source-formatter.properties");

        if (!file.exists()) {
            String content = "upgrade.to.liferay.version=7.4\n" + "upgrade.to.release.version=" + targetRelease + "\n";

            Files.write(file.toPath(), content.getBytes());

            _log.info("Created source-formatter.properties targeting " + targetRelease);
        } else {
            _log.info("source-formatter.properties already exists. Skipping creation");
        }
    }

    public void updateGradleProperties(String directory) throws Exception {
        File file = new File(directory, "gradle.properties");

        String propertyToAdd = "com.liferay.source.formatter.version=latest.release";

        if (file.exists()) {
            List<String> lines = Files.readAllLines(file.toPath());

            boolean exists = lines.stream().anyMatch(line -> line.contains("com.liferay.source.formatter.version"));

                if (!exists) {
                    String entry = "\n" + propertyToAdd + "\n";

                    Files.write(file.toPath(), entry.getBytes(), StandardOpenOption.APPEND);

                    _log.info("Added source formatter version to gradle.properties");
                } else {
                    _log.info("Source formatter version already defined in gradle.properties.");
                }
            }
        }



        private static final Logger _log = Logger.getLogger(SourceFormatterConfigurator.class.getName());
    }


