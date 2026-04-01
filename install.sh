#!/bin/bash

# Configuration
install_file_path=".liferay-upgrades-setup-version"
file_name="upgrade-setup-version.jar"
repo_name="upgrade-setup-version"
github_org="liferay-upgrades"
alias_name="usv"
function_name="upgrade_setup_version_project"

function getLatestSnapshot() {
    echo "Fetching latest release from GitHub..."
    location="$(curl -I -s https://github.com/${github_org}/${repo_name}/releases/latest | grep -i "location:" | cut -d " " -f 2 | tr -d '\r\n')"
    tag="${location##*/}"

    if [ -z "$tag" ]; then
        echo "Error: Could not find latest release tag."
        exit 1
    fi

    url="https://github.com/${github_org}/${repo_name}/releases/download/${tag}/${file_name}"

    echo "Starting download from ${url}"

    mkdir -p ~/$install_file_path
    cd ~/$install_file_path || exit
    curl -L -o $file_name "$url"

    if [ $? -eq 0 ]; then
        echo "Successfully downloaded $file_name to ~/$install_file_path"
    else
        echo "Error: Download failed."
        exit 1
    fi
}

function writeFunction() {
    echo -e "function $1 {\n    $2\n}"
}

function addAliasOnBashrc() {
    # Only add alias and function if not already present
    if ! grep -q "$function_name" ~/.bashrc; then
        echo "Adding $alias_name alias and $function_name function to ~/.bashrc..."

        startupFunctionBody="java -jar ~/$install_file_path/$file_name \"\$@\""
        startupFunction=$(writeFunction "$function_name" "$startupFunctionBody")

        aliasFunctionBody="alias $alias_name=\"$function_name\""
        aliasFunctionName="${function_name}_alias"
        aliasFunction=$(writeFunction "$aliasFunctionName" "$aliasFunctionBody")

        {
            echo -e "\n\n# Liferay Upgrade Setup Version Tool"
            echo "$startupFunction"
            echo -e "\n$aliasFunction"
            echo -e "\n$aliasFunctionName"
        } >> ~/.bashrc

        echo "Installation complete! Please restart your terminal or run: source ~/.bashrc"
        echo "You can now use the tool with the '$alias_name' command."
    else
        echo "Tool is already configured in ~/.bashrc. Skipping."
    fi
}

getLatestSnapshot
addAliasOnBashrc
