# UPGRADE SETUP VERSION

This project is a Java-based automation tool designed to streamline the upgrade setup process for Liferay Workspace projects to version 7.4 and beyond (including Jakarta EE migration). It orchestrates a series of refactoring steps, shell commands, and Git commits to ensure a consistent and traceable upgrade path.

## Purpose

The tool automates the repetitive and error-prone manual steps required during a Liferay upgrade, such as updating properties files, refactoring Gradle configurations, cleaning up BND headers, and executing specialized upgrade tasks like Source Formatter and Jakarta migration.

## Prerequisites

To run this tool, you need:
- **Java 11+** (JDK)
- **Gradle** (or use the provided `./gradlew` wrapper)
- **Liferay Blade CLI** (installed and available in the system PATH)
- **Git** (configured in the target workspace)
- **Linux/macOS environment** (uses shell commands like `sed`, `grep`, and `find`)

## Automated Steps

The tool performs the following 13 steps in order:

1.  **Update `gradle.properties`**: Sets `liferay.workspace.product` and removes obsolete target platform properties.
2.  **Update `docker-compose.yml`**: Updates the Liferay DXP image tag.
3.  **Update `settings.gradle`**: Updates the `com.liferay.gradle.plugins.workspace` version.
4.  **Update Gradle Wrapper**: Updates `distributionUrl` in `gradle-wrapper.properties`.
5.  **Refactor `build.gradle` Dependencies**: Migrates legacy configurations (e.g., `compile` to `compileOnly`).
6.  **Update Portal API**: Replaces `release.portal.api` with `release.dxp.api`.
7.  **Remove Compatibility Properties**: Deletes `sourceCompatibility` and `targetCompatibility` settings.
8.  **Refactor `bnd.bnd`**: Removes hardcoded `bundle-version` constraints.
9.  **Configure Source Formatter**: Creates `source-formatter.properties` and updates `gradle.properties`.
10. **Run Source Formatter**: Executes `blade gw formatSource` to update the gradle,xml,bnd dependencies on all modules.
11. **Build Service**: Runs `blade gw buildService` for any module containing `service.xml`.
12. **Build REST**: Runs `blade gw buildRest` for any module containing `rest-config.yaml`.
13. **Jakarta EE Upgrade**: Executes `blade gw upgradeJakarta` for modern Liferay versions (DXP 2025.Q3+).

*Note: Each step is followed by an atomic Git commit using a standardized message pattern.*

## How to Run

### 1. Build the Project
Compile the project and generate the executable JAR:
```bash
./gradlew clean build
```

### 2. Execute the Tool
Run the tool by pointing it to a customer's Liferay Workspace.

```bash
java -jar build/libs/upgrade-setup-version.jar \
  --ticket "ECU-1234" \
  --folder "/path/to/liferay-workspace" \
  --liferay-version "dxp-2024.q4.0" \
  --target-release "2024.q4.0" \
  --plugin-version "11.0.1" \
  --gradle-version "8.5" \
  --docker-compose "2024.q4.0"
```

### Parameters

| Option | Shorthand | Description | Required |
| :--- | :--- | :--- | :--- |
| `--ticket` | `-t` | Jira ticket ID (used for commit messages) | Yes |
| `--folder` | `-f` | Path to the Liferay Workspace root | Yes |
| `--liferay-version`| `-l` | The target Liferay version (e.g., `dxp-2024.q4.0`) | Yes |
| `--target-release` | `-tr` | Target release for Source Formatter (e.g., `2024.q4.0`) | Yes |
| `--plugin-version` | `-p` | New Liferay Workspace plugin version | Yes |
| `--gradle-version` | `-g` | New Gradle version (e.g., `8.5`) | Yes |
| `--docker-compose` | `-d` | New Docker image tag | No |

## Project Structure

- `com.liferay.upgrades.main.Main`: The orchestrator and CLI entry point.
- `com.liferay.upgrades.project.dependency.gradle`: Logic for Gradle file refactoring and Service/REST builders.
- `com.liferay.upgrades.project.dependency.sourceformatter`: Logic for configuring and running the Source Formatter.
- `com.liferay.upgrades.project.dependency.jakarta`: Logic for the Jakarta EE migration tool.
- `com.liferay.upgrades.project.dependency.git`: Handles automated commits.

## Contributing

When adding new refactoring steps:
1. Create a specific runner/refactorer class in the appropriate package.
2. Implement the logic using `ProcessBuilder` for shell commands or standard Java File I/O.
3. Integrate the step into `Main.java` following the `Step N` pattern.
4. Ensure the commit message follows the pattern: `"{Ticket} {Action} in {File}"`.
