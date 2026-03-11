package com.liferay.upgrades.project.dependency.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class UpdateDockerCompose {

    public String run(String directory, String newVersion) throws Exception {
        Path projectRoot = Paths.get(directory);

        Path path = _findDockerComposeFile(projectRoot);

        if (path == null) {
            _log.info("No docker-compose file (.yml or .yaml) found in " + directory + " or its subdirectories. Skipping step.");
            return null;
        }

        _log.info("Updating docker-compose file at: " + path.toAbsolutePath());

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

                _log.info("Updated Docker image tag from " + oldTag + " to " + newVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (oldTag != null) {
            Files.write(path, updatedLines);
        }

        return oldTag;
    }

    private Path _findDockerComposeFile(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> !Files.isDirectory(p))
                       .filter(p -> {
                           String pathString = p.toAbsolutePath().toString();

                           if (pathString.contains("/build/") || pathString.contains("/.gradle/") ||
                               pathString.contains("/.idea/") || pathString.contains("/bin/") ||
                               pathString.contains("/.git/")) {
                               return false;
                           }

                           String name = p.getFileName().toString();
                           return name.equalsIgnoreCase("docker-compose.yml") || 
                                  name.equalsIgnoreCase("docker-compose.yaml");
                       })
                       .findFirst()
                       .orElse(null);
        }
    }


    private static final String _dockerImagePrefix = "image: liferay/dxp:";

    private static final Logger _log = Logger.getLogger(UpdateDockerCompose.class.getName());
}
