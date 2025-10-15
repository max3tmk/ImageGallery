#!/bin/bash
# PushAll.sh
# Script to push changes from Monorepo into individual module repositories
# and create pull requests for each module with changes.

# Exit on error
set -e

# Monorepo branch
MONO_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current Monorepo branch: $MONO_BRANCH"

# List of module directories and their remote URLs
declare -A MODULES
MODULES["APIGatewayService"]="git@github.com:max3tmk/APIGatewayService.git"
MODULES["AuthenticationService"]="git@github.com:max3tmk/AuthenticationService.git"
MODULES["Common"]="git@github.com:max3tmk/Common.git"
MODULES["ImageService"]="git@github.com:max3tmk/ImageService.git"
MODULES["frontend"]="git@github.com:max3tmk/frontend.git"

for module in "${!MODULES[@]}"; do
    echo "Processing module: $module"
    REMOTE_URL=${MODULES[$module]}

    if git diff --name-only HEAD~1 HEAD | grep -q "^$module/"; then
        echo "Changes detected in $module"

        cd $module

        git checkout -B "$MONO_BRANCH"

        git add -A

        if ! git diff --cached --quiet; then
            git commit -m "Sync from Monorepo branch $MONO_BRANCH"
        else
            echo "No staged changes in $module"
        fi

        git push -u "$REMOTE_URL" "$MONO_BRANCH" --force

        # Create Pull Request using GitHub CLI (gh)
        # Title: branch name, Body: list of commits
        PR_BODY=$(git log --format="%h %s" origin/main..HEAD)
        gh pr create --repo "$REMOTE_URL" --base main --head "$MONO_BRANCH" --title "$MONO_BRANCH" --body "$PR_BODY"

        cd ..
    else
        echo "No changes in $module"
    fi
done

echo "PushAll completed."