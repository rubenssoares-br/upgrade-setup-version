package com.liferay.upgrades.project.dependency.gradle;

import java.io.File;
import java.util.logging.Logger;
public class BuildGradleRefactorer {

    public void refactorTaskDependencies(String directory) throws Exception {
        String[] actions = {
            "s/\\bcompile\\b/compileOnly/g",
            "s/\\btestCompile\\b/testCompileOnly/g",
            "s/\\bruntime\\b/runtimeOnly/g",
            "s/\\btestRuntime\\b/testRuntimeOnly/g"
        };
        for (String action : actions) {
            _executeShell(directory, _generateCommand(action));
        }
    }

    public boolean refactorPortalApi(String directory) throws Exception {
        if (_hasProperty(directory, "release.portal.api")) {
            _executeShell(directory, _generateCommand("s/release\\.portal\\.api/release.dxp.api/g"));

            return true;
        }

        return false;
    }

    public boolean removeCompatibilityProperties(String directory) throws Exception {

        if (_hasProperty(directory, "sourceCompatibility") || _hasProperty(directory, "targetCompatibility")) {

            _log.info("Compatibility properties found. Proceeding with removal...");

            _executeShell(directory, _generateCommand("/sourceCompatibility/d"));

            _executeShell(directory, _generateCommand("/targetCompatibility/d"));

            return true;
        } else {
            _log.info("No sourceCompatibility or targetCompatibility properties found. Skipping step.");

            return false;
        }
    }

    private boolean _hasProperty(String directory, String propertyName) throws Exception {

        String checkCommand = String.format("grep -rql --include=\"build.gradle\" \"%s\" .", propertyName);

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", checkCommand);

        processBuilder.directory(new File(directory));

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        return exitCode == 0;
    }

    private String _generateCommand(String sedAction) {
        return String.format(
                "find | grep -i \"build.gradle\"| while read origin; do cat \"$origin\"| sed -e '%s' > \"$origin\".new; rm \"$origin\"; mv \"$origin\".new \"$origin\"; done",
                sedAction
        );
    }

    private void _executeShell(String directory, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new java.io.File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("Command failed with exit code: " + exitCode);
        }
    }
    private static final Logger _log = Logger.getLogger(BuildGradleRefactorer.class.getName());
}
