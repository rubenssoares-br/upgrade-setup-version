package com.liferay.upgrades.project.dependency.bnd;

import java.io.File;
import java.util.logging.Logger;

public class BndRefactorer {


    public boolean run(String directory) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String sedCommand;

        if (!_hasBundleVersionConstraints(directory)) {
            _log.info("No 'bundle-version' attribute constraints found in .bnd files. Skipping.");
            return false;
        }

        if (os.contains("linux")) {
            _log.info("Linux/Unix detected for .bnd refactor.");

            sedCommand = "sed -i -E 's/;?bundle-version=\"[^\"]+\"//g'";
        } else if (os.contains("mac")) {
            _log.info("Mac OS detected for .bnd refactor.");

            sedCommand = "sed -i '' -E 's/;?bundle-version=\"[^\"]+\"//g'";
        } else {
            throw new UnsupportedOperationException("OS not supported for .bnd refactor: " + os);
        }

        String command = String.format(
                "find . -type d \\( -name .gradle -o -name build -o -name .git \\) -prune -o -type f -name \"*.bnd\" -print | while read -r file; do %s \"$file\"; done",
                sedCommand
        );

        _executeShell(directory, command);
        return true;
    }

    private boolean _hasBundleVersionConstraints(String directory) throws Exception {
        String checkCommand = "grep -rqlE --include=\"*.bnd\" --exclude-dir={.gradle,build,.git} \"bundle-version=\\\"\" .";

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", checkCommand);

        processBuilder.directory(new File(directory));

        Process process = processBuilder.start();
        return process.waitFor() == 0;
    }


    private void _executeShell(String directory, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("BND Refactor command failed with exit code: " + exitCode);
        } else {
            _log.info("Successfully refactored bnd.bnd files.");
        }

    }

    private static final Logger _log = Logger.getLogger(BndRefactorer.class.getName());
}
