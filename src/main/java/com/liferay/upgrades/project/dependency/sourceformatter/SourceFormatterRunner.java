package com.liferay.upgrades.project.dependency.sourceformatter;

import java.io.File;
import java.util.logging.Logger;

public class SourceFormatterRunner {

    public void run(String directory) throws Exception {
        _log.info("Running source-formatter automation...");

        String modulesDirectory = directory + File.separator + "modules";

        File modulesFolder = new File(modulesDirectory);

        if (!modulesFolder.exists() || !modulesFolder.isDirectory()) {
            _log.warning("Modules directory not found at: " + modulesDirectory);

            return;
        }

        String command = "blade gw formatSource -Pjava.parser.enabled=false -Psource.check.category.names=Upgrade -Psource.file.extensions=gradle,xml,bnd";

        _executeShell(modulesDirectory, command);
    }

    private void _executeShell(String directory, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("Source formatter command failed with exit code: " + exitCode);
        } else {
            _log.info("Successfully ran source formatter.");
        }
    }

    private static final Logger _log = Logger.getLogger(SourceFormatterRunner.class.getName());
}
