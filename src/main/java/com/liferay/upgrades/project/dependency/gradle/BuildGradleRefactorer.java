package com.liferay.upgrades.project.dependency.gradle;

import java.util.logging.Logger;
public class BuildGradleRefactorer {

    public void run(String directory) throws Exception {
        String[] commands = {
                "find | grep -i \"build.gradle\"| while read origin; do echo \"$origin\"; cat \"$origin\"| sed -e 's/\\bcompile\\b/compileOnly/g' > \"$origin\".new; rm \"$origin\"; mv \"$origin\".new \"$origin\"; done",
                "find | grep -i \"build.gradle\"| while read origin; do echo \"$origin\"; cat \"$origin\"| sed 's/\\btestCompile\\b/testCompileOnly/g' > \"$origin\".new; rm \"$origin\"; mv \"$origin\".new \"$origin\"; done",
                "find | grep -i \"build.gradle\"| while read origin; do echo \"$origin\"; cat \"$origin\"| sed -e 's/\\bruntime\\b/runtimeOnly/g' > \"$origin\".new; rm \"$origin\"; mv \"$origin\".new \"$origin\"; done",
                "find | grep -i \"build.gradle\"| while read origin; do echo \"$origin\"; cat \"$origin\"| sed 's/\\btestRuntime\\b/testRuntimeOnly/g' > \"$origin\".new; rm \"$origin\"; mv \"$origin\".new \"$origin\"; done"
        };

        for (String cmd : commands) {
            _log.info("Running shell refactor...");
            _executeShell(directory, cmd);
        }
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
