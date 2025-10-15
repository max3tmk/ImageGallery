#!/bin/bash
# Script to push changes from Monorepo into individual module repositories.
# Feature branches: create pull requests.
# Main branch: push without pull request. Any local changes are temporarily stashed.

set -e

# Determine current Monorepo branch
MONO_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current Monorepo branch: $MONO_BRANCH"

# List of module directories and their remote URLs
declare -A MODULES
MODULES["APIGatewayService"]="git@github.com:max3tmk/APIGatewayService.git"
MODULES["AuthenticationService"]="git@github.com:max3tmk/AuthenticationService.git"
MODULES["Common"]="git@github.com:max3tmk/Common.git"
MODULES["ImageService"]="git@github.com:max3tmk/ImageService.git"
MODULES["frontend"]="git@github.com:max3tmk/frontend.git"

STASHED=false

if [ "$MONO_BRANCH" == "main" ]; then
    echo "You are on main branch."

    # Check for local changes compared to origin/main
    if ! git diff --quiet origin/main; then
        echo "Local changes detected. Stashing before push..."
        git stash push -m "Temp stash before PushAll"
        STASHED=true
    fi
fi

for module in "${!MODULES[@]}"; do
    echo "Processing module: $module"
    REMOTE_URL=${MODULES[$module]}

    # Check if there are changes in this module
    if git diff --name-only HEAD~1 HEAD | grep -q "^$module/"; then
        echo "Changes detected in $module"

        cd $module

        # Create or switch to branch with same name as Monorepo branch
        git checkout -B "$MONO_BRANCH"

        git add -A

        # Commit changes if any
        if ! git diff --cached --quiet; then
            git commit -m "Sync from Monorepo branch $MONO_BRANCH"
        else
            echo "No staged changes in $module"
        fi

        # Push to module remote
        git push -u "$REMOTE_URL" "$MONO_BRANCH" --force

        # Create Pull Request only if not main
        if [ "$MONO_BRANCH" != "main" ]; then
            echo "Creating pull request for $module..."
            PR_BODY=$(git log --format="%h %s" origin/main..HEAD)
            gh pr create --repo "$REMOTE_URL" --base main --head "$MONO_BRANCH" --title "$MONO_BRANCH" --body "$PR_BODY"
        else
            echo "Main branch detected, no pull request created."
        fi

        cd ..
    else
        echo "No changes in $module"
    fi
done

# Restore stashed changes if any
if [ "$STASHED" = true ]; then
    echo "Restoring stashed local changes..."
    git stash pop
fi

echo "PushAll completed."