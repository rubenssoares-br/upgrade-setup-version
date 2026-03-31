package com.liferay.upgrades.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.liferay.upgrades.project.dependency.bnd.BndRefactorer;
import com.liferay.upgrades.project.dependency.docker.UpdateDockerCompose;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.gradle.BuildGradleRefactorer;
import com.liferay.upgrades.project.dependency.gradle.BuildRestRefactorer;
import com.liferay.upgrades.project.dependency.gradle.BuildServiceRefactorer;
import com.liferay.upgrades.project.dependency.gradle.UpdateGradleProperties;
import com.liferay.upgrades.project.dependency.gradle.UpdateGradleWrapper;
import com.liferay.upgrades.project.dependency.gradle.UpdateSettingsGradle;
import com.liferay.upgrades.project.dependency.jakarta.JakartaUpgradeRunner;
import com.liferay.upgrades.project.dependency.sourceformatter.SourceFormatterConfigurator;
import com.liferay.upgrades.project.dependency.sourceformatter.SourceFormatterRunner;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            VersionOptions versionOptions = _resolveOptions(args);

            if (!versionOptions.directory.isEmpty() && !versionOptions.liferayVersion.isEmpty() && !versionOptions.ticket.isEmpty()) {

                _log.info("Starting upgrade process...");

                GitHandler gitHandler = new GitHandler();

                UpdateGradleProperties updateGradleProperties = new UpdateGradleProperties();

                String oldVersionLiferay = updateGradleProperties.run(versionOptions.directory, versionOptions.liferayVersion);

                if (oldVersionLiferay != null) {
                    _log.info("Step 1 (gradle.properties) complete.");

                    _log.info("Commiting Step 1...");

                    String commitMsgStep1 = String.format("%s Update liferay.workspace.product from %s to %s in gradle.properties", versionOptions.ticket, oldVersionLiferay, versionOptions.liferayVersion);

                    gitHandler.commit(versionOptions.directory, commitMsgStep1);
                } else {
                    _log.info("Step 1 (gradle.properties) already up to date.");
                }

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

                if (oldPluginVersion != null) {
                    String commitMsgStep3 = String.format("%s Update com.liferay.gradle.plugins.workspace from %s to %s in settings.gradle", versionOptions.ticket, oldPluginVersion, versionOptions.pluginsVersion);

                    gitHandler.commit(versionOptions.directory, commitMsgStep3);
                } else {
                    _log.info("Step 3 (settings.gradle) already up to date.");
                }

                _log.info("Step 4 Updating Gradle Wrapper...");

                if (versionOptions.gradleVersion != null) {
                    UpdateGradleWrapper updateGradleWrapper = new UpdateGradleWrapper();

                    String oldGradleVersion = updateGradleWrapper.run(versionOptions.directory, versionOptions.gradleVersion);

                    if (oldGradleVersion != null) {
                        String commitMsgStep4 = String.format("%s Update distributionUrl to Gradle %s in gradle-wrapper.properties", versionOptions.ticket, versionOptions.gradleVersion);

                        gitHandler.commit(versionOptions.directory, commitMsgStep4);
                    }
                } else {
                    _log.info("Skipping Step 4: gradle-version not provided.");
                }

                _log.info("Step 5 refactoring deprecated tasks dependencies in build.gradle files...");

                BuildGradleRefactorer buildGradleRefactorer = new BuildGradleRefactorer();

                buildGradleRefactorer.refactorTaskDependencies(versionOptions.directory);

                String commitMsgStep5 = String.format("%s Refactor build.gradle: migrate legacy dependency configurations", versionOptions.ticket);

                gitHandler.commit(versionOptions.directory, commitMsgStep5);

                _log.info("Updating Portal API to DXP API...");

                boolean portalApiChanged = buildGradleRefactorer.refactorPortalApi(versionOptions.directory);

                if (portalApiChanged) {
                    String commitMsgStep6 = String.format("%s Update release.portal.api to release.dxp.api in build.gradle files", versionOptions.ticket);

                    gitHandler.commit(versionOptions.directory, commitMsgStep6);
                }

                _log.info("Step 7: Checking for source/target compatibility properties...");

                boolean changed = buildGradleRefactorer.removeCompatibilityProperties(versionOptions.directory);

                if (changed) {

                    String commitMsgStep7 = String.format("%s Remove sourceCompatibility and targetCompatibility from build.gradle files", versionOptions.ticket);

                    gitHandler.commit(versionOptions.directory, commitMsgStep7);
                }

                _log.info("Step 8: Refactoring bnd.bnd files...");

                BndRefactorer bndRefactorer = new BndRefactorer();

                boolean bndChanged = bndRefactorer.run(versionOptions.directory);

                if (bndChanged) {
                    String commitMsgStep8 = String.format("%s Refactor bnd.bnd: remove hardcoded bundle-version constraints", versionOptions.ticket);

                    gitHandler.commit(versionOptions.directory, commitMsgStep8);
                }

                _log.info("Step 9: Configuring SourceFormatter properties...");

                SourceFormatterConfigurator sourceFormatterConfigurator = new SourceFormatterConfigurator();

                sourceFormatterConfigurator.configureSourceFormatter(versionOptions.directory, versionOptions.targetRelease);

                sourceFormatterConfigurator.updateGradleProperties(versionOptions.directory);

                String commitMsgStep9 = String.format("%s Workspace: Configure source-formatter.properties for %s", versionOptions.ticket, versionOptions.targetRelease);

                gitHandler.commit(versionOptions.directory, commitMsgStep9);

                _log.info("Step 10: Running SourceFormatter runner...");

                SourceFormatterRunner sourceFormatterRunner = new SourceFormatterRunner();

                sourceFormatterRunner.run(versionOptions.directory);

                String commitMsgStep10 = String.format("%s SF automation update dependencies", versionOptions.ticket);

                gitHandler.commit(versionOptions.directory, commitMsgStep10);

                _log.info("Step 11: Running buildService for modules with service.xml...");

                BuildServiceRefactorer buildServiceRefactorer = new BuildServiceRefactorer();

                List<File> serviceModules = buildServiceRefactorer.findServiceModules(versionOptions.directory);

                for (File module : serviceModules) {
                    buildServiceRefactorer.run(module.getAbsolutePath());

                    String commitMsgStep11 = String.format("%s buildService in %s module", versionOptions.ticket, module.getName());

                    gitHandler.commit(versionOptions.directory, commitMsgStep11);
                }

                _log.info("Step 12: Running buildRest for modules with rest-config.yaml...");

                BuildRestRefactorer buildRestRefactorer = new BuildRestRefactorer();

                List<File> restModules = buildRestRefactorer.findRestModules(versionOptions.directory);

                for (File module : restModules) {
                    buildRestRefactorer.run(module.getAbsolutePath());

                    String commitMsgStep12 = String.format("%s buildRest in %s module", versionOptions.ticket, module.getName());

                    gitHandler.commit(versionOptions.directory, commitMsgStep12);
                }

                _log.info("Step 13: Upgrading to Jakarta EE...");

                JakartaUpgradeRunner jakartaUpgradeRunner = new JakartaUpgradeRunner();

                jakartaUpgradeRunner.run(versionOptions.directory);

                String commitMsgStep13 = String.format("%s Upgrade to Jakarta EE", versionOptions.ticket);

                gitHandler.commit(versionOptions.directory, commitMsgStep13);

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
                \t--gradle-version or -g to Set the new Gradle version
                \t--liferay-version or -l to set the new Liferay upgrade version (Required)
                \t--docker-compose or -d to set the new image liferay version in docker compose
                \t--folder or -f to specify the path for the liferay workspace (Required)
                \t--target-release or -tr to Set the target release for source-formatter
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
                names = {"-g", "--gradle-version"},
                description = "Set the new Gradle version)"
        )
        String gradleVersion;

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

        @Parameter(
                names = {"-tr", "--target-release"},
                description = "Set the target release for source-formatter",
                required = true
        )
        String targetRelease;
    }

    private static final Logger _log = Logger.getLogger(Main.class.getName());
}
