package com.liferay.upgrades.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.gradle.UpdateGradleProperties;

import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            VersionOptions versionOptions = _resolveOptions(args);

            if (!versionOptions.directory.isEmpty() && !versionOptions.liferayVersion.isEmpty() && !versionOptions.ticket.isEmpty()) {

                _log.info("Starting upgrade process...");

                UpdateGradleProperties updateGradleProperties = new UpdateGradleProperties();

                String oldVersionLiferay = updateGradleProperties.run(versionOptions.directory, versionOptions.liferayVersion);

                _log.info("Step 1 (gradle.properties) complete.");

                _log.info("Commiting Step 1...");

                String commitMsgStep1 = String.format("%s Update liferay.workspace.product from %s to %s in gradle.properties", versionOptions.ticket, oldVersionLiferay, versionOptions.liferayVersion);

                GitHandler gitHandler = new GitHandler();

                gitHandler.commit(versionOptions.directory, commitMsgStep1);

            }
        } catch (Exception  exception) {
            if (exception instanceof ParameterException) {
                _log.info(_generateOptionsHelp());
            }
            else throw new RuntimeException(exception);
        }
    }

    private static String _generateOptionsHelp() {
        return """
                The available options are:
                \t--ticket or -t to set the Jira ticket ID (Required)
                \t--liferay-version or -l to set the new Liferay upgrade version (Required)
                \t--folder or -f to specify the path for the liferay workspace (Required)
               """;
    }

    private static VersionOptions _resolveOptions(String[] args) {
        VersionOptions versionOptions = new VersionOptions();

        JCommander jCommander = JCommander.newBuilder()
                .addObject(versionOptions)
                .build();

        jCommander.parse(args);

        return versionOptions;
    }

    private static class VersionOptions {

        @Parameter(
                names = {"-t", "--ticket"},
                description = "Jira ticket ID",
                required = true
        )
        String ticket;

         @Parameter(
            names = {"-l", "--liferay-version"},
            description = "Set the new Liferay upgrade version",
            required = true
         )
        String liferayVersion;

        @Parameter(
                names = {"-f", "--folder"},
                description = "Specify the path for the liferay workspace",
                required = true
        )
        String directory;
    }

    private static final Logger _log = Logger.getLogger(Main.class.getName());
}
