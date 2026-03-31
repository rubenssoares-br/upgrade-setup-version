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
            _log.info("source-formatter.properties already exists. Updating properties...");

            List<String> lines = Files.readAllLines(file.toPath());
            boolean liferayVersionFound = false;
            boolean releaseVersionFound = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.startsWith("upgrade.to.liferay.version=")) {
                    lines.set(i, "upgrade.to.liferay.version=7.4");
                    liferayVersionFound = true;
                } else if (line.startsWith("upgrade.to.release.version=")) {
                    lines.set(i, "upgrade.to.release.version=" + targetRelease);
                    releaseVersionFound = true;
                }
            }

            if (!liferayVersionFound) {
                lines.add("upgrade.to.liferay.version=7.4");
            }
            if (!releaseVersionFound) {
                lines.add("upgrade.to.release.version=" + targetRelease);
            }

            Files.write(file.toPath(), lines);
            _log.info("Updated source-formatter.properties with Liferay 7.4 and release " + targetRelease);
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


