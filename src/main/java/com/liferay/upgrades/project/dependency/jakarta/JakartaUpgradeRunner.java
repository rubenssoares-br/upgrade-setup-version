package com.liferay.upgrades.project.dependency.jakarta;

import java.io.File;
import java.util.logging.Logger;

public class JakartaUpgradeRunner {

    public void run(String directory) throws Exception {
        _log.info("Running Jakarta EE upgrade automation...");

        String command = "blade gw upgradeJakarta";

        _executeShell(directory, command);
    }

    private void _executeShell(String directory, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("Jakarta upgrade command failed with exit code: " + exitCode);
        } else {
            _log.info("Successfully ran Jakarta upgrade tool.");
        }
    }

    private static final Logger _log = Logger.getLogger(JakartaUpgradeRunner.class.getName());
}
