package com.liferay.upgrades.project.dependency.gradle;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class UpdateGradleProperties {

    public String run(String directory, String newVersion) throws Exception {
        Path path = Paths.get(directory, "gradle.properties");

        if (!Files.exists(path)) {
            throw new FileNotFoundException("gradle.properties not found at: " + path.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(path);

        List<String> updatedLines = new ArrayList<>();

        String oldVersion = "";
        boolean productFound = false;

        for (String line: lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                updatedLines.add(line);
                continue;
            }

            if (_isLegacyProperty(trimmedLine)) {
                _log.info("Removing legacy property: " + trimmedLine.split("=")[0]);
                continue;
            }

            if (trimmedLine.startsWith(_productProperty + "=")) {
                oldVersion = trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();

                updatedLines.add(_productProperty + "=" + newVersion);
                productFound = true;
                _log.info("Updated " + _productProperty + "to " + newVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (!productFound) {
            updatedLines.add(_productProperty + "=" + newVersion);
            _log.info("Added " + _productProperty + "=" + newVersion + " to end of file.");
        }

        Files.write(path, updatedLines);

        return oldVersion.isEmpty() ? "legacy" : oldVersion;
    }


    private static final Logger _log = Logger.getLogger(UpdateGradleProperties.class.getName());

    private static final String _productProperty = "liferay.workspace.product";

    private static final Set<String> _legacyProperties = Set.of(
        "liferay.workspace.target.platform.version",
        "liferay.workspace.docker.image.liferay",
        "liferay.workspace.bundle.url",
        "app.server.tomcat.version"
    );

    private boolean _isLegacyProperty(String line) {
        return _legacyProperties.stream()
                .anyMatch(key -> line.startsWith(key + "="));
    }
}
