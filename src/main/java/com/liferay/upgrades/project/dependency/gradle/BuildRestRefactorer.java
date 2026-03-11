package com.liferay.upgrades.project.dependency.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BuildRestRefactorer {

    public List<File> findRestModules(String directory) {
        List<File> restModules = new ArrayList<>();
        File modulesDir = new File(directory, "modules");

        if (modulesDir.exists() && modulesDir.isDirectory()) {
            _findRestConfigYaml(modulesDir, restModules);
        }

        return restModules;
    }

    private void _findRestConfigYaml(File directory, List<File> restModules) {
        String name = directory.getName();

        if (name.equals("bin") || name.equals("build") || name.equals(".gradle") || name.equals(".idea")) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                _findRestConfigYaml(file, restModules);
            } else if (file.getName().equals("rest-config.yaml")) {
                restModules.add(file.getParentFile());
            }
        }
    }

    public void run(String modulePath) throws Exception {
        _log.info("Running buildRest in " + modulePath);
        _executeShell(modulePath, "blade gw buildRest");
    }

    private void _executeShell(String directory, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("Command '" + command + "' failed with exit code: " + exitCode);
        }
    }

    private static final Logger _log = Logger.getLogger(BuildRestRefactorer.class.getName());
}
