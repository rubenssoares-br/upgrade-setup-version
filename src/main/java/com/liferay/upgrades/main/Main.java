package com.liferay.upgrades.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.liferay.upgrades.project.dependency.docker.UpdateDockerCompose;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.gradle.UpdateGradleProperties;
import com.liferay.upgrades.project.dependency.gradle.UpdateSettingsGradle;

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

                _log.info("Step 2 Updating docker-compose.yml...");

                UpdateDockerCompose updateDockerCompose = new UpdateDockerCompose();
                String oldDockerTag = updateDockerCompose.run(versionOptions.directory, versionOptions.dockerCompose);

                if (oldDockerTag != null) {
                    String commitMsgStep2 = String.format("%s Update liferay/dxp image from %s to %s in docker-compose.yml", versionOptions.ticket, oldDockerTag, versionOptions.dockerCompose);

                    gitHandler.commit(versionOptions.directory, commitMsgStep2);
                }

                _log.info("Step 3 Updating settings.gradle...");

                UpdateSettingsGradle updateSettingsGradle = new UpdateSettingsGradle();

                String oldPluginVersion = updateSettingsGradle.run(versionOptions.directory, versionOptions.pluginsVersion);

                String commitMsgStep3 = String.format("%s Update com.liferay.gradle.plugins.workspace from %s to %s in settings.gradle", versionOptions.ticket, oldPluginVersion, versionOptions.pluginsVersion);

                gitHandler.commit(versionOptions.directory, commitMsgStep3);

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
                \t--plugin-version or -p to set the new Liferay workspace plugin version
                \t--liferay-version or -l to set the new Liferay upgrade version (Required)
                \t--docker-compose or -d to set the new image liferay version in docker compose
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
                names = {"-p", "--plugin-version"},
                description = "Set the new liferay workspace plugin version",
                required = true
        )
        String pluginsVersion;

         @Parameter(
            names = {"-l", "--liferay-version"},
            description = "Set the new Liferay upgrade version",
            required = true
         )
        String liferayVersion;

        @Parameter(
                names = {"-d", "--docker-compose"},
                description = "Set the new image liferay version in docker compose"
        )
        String dockerCompose;

        @Parameter(
                names = {"-f", "--folder"},
                description = "Specify the path for the liferay workspace",
                required = true
        )
        String directory;
    }

    private static final Logger _log = Logger.getLogger(Main.class.getName());
}
