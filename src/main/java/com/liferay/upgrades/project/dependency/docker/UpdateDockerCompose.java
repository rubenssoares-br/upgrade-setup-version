package com.liferay.upgrades.project.dependency.docker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateDockerCompose {

    public String run(String directory, String newVersion) throws Exception {

        String[] possibleFiles = {"docker-compose.yml", "docker-compose.yaml"};

        Path path = null;

        for (String fileName : possibleFiles) {
            Path tempPath = Paths.get(directory, fileName);
            if (Files.exists(tempPath)) {
                path = tempPath;
                break;
            }
        }

        if (path == null) {
            _log.warning("No docker-compose file (.yml or .yaml) found in " + directory + ". Skipping step.");
            return null;
        }

        List<String> lines = Files.readAllLines(path);

        List<String> updatedLines = new ArrayList<>();

        String oldTag = null;

        Pattern pattern = Pattern.compile("image:\\s*liferay/dxp:(.*)");

        for (String line: lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                oldTag = matcher.group(1).trim();

                String indentation = line.substring(0, line.indexOf("image:"));
                updatedLines.add(indentation + _dockerImagePrefix + newVersion);

                _log.info("Updated Docker image tag from " + oldTag + " to" + newVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (oldTag != null) {
            Files.write(path, updatedLines);
        }

        return oldTag;
    }


    private static final String _dockerImagePrefix = "image: liferay/dxp:";

    private static final Logger _log = Logger.getLogger(UpdateDockerCompose.class.getName());
}
