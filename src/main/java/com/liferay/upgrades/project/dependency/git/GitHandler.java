package com.liferay.upgrades.project.dependency.git;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class GitHandler {

    public void commit(String directory, String message) throws Exception {

        _executeCommand(directory, "git", "add", ".");

        _executeCommand(directory, "git", "commit", "-m", message);

        _log.info("Git commit successful: " + message);

    }

    private void _executeCommand(String directory, String... command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(Paths.get(directory).toFile());

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                _log.fine("GIT: " + line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Git command failed with exit code " + exitCode);
        }

    }

    private static final Logger _log = Logger.getLogger(GitHandler.class.getName());
}
