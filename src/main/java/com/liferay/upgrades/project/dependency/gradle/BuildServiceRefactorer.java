package com.liferay.upgrades.project.dependency.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BuildServiceRefactorer {

    public List<File> findServiceModules(String directory) {
        List<File> serviceModules = new ArrayList<>();

        File modulesDir = new File(directory, "modules");

        if (modulesDir.exists() && modulesDir.isDirectory()) {
            _findServiceXml(modulesDir, serviceModules);
        }

        return serviceModules;
    }

    private void _findServiceXml(File directory, List<File> serviceModules) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                _findServiceXml(file, serviceModules);
            } else if (file.getName().equals("service.xml")) {
                serviceModules.add(file.getParentFile());
            }
        }
    }

    public void run(String modulePath) throws Exception {
        _log.info("Running buildService in " + modulePath);
        _executeShell(modulePath, "blade gw buildService");
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

    private static final Logger _log = Logger.getLogger(BuildServiceRefactorer.class.getName());
}
