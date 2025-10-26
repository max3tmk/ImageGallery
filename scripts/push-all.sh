#!/usr/bin/env bash
# Push changes from Monorepo into individual module repositories
# Preserves commit history using git subtree
# For main branch: push directly without PR
# For feature branches: push to module branch and create PR
# Safe to re-run multiple times

set -e

# Ensure running from the root of the repository
if [ ! -d ".git" ]; then
    echo "ERROR: This script must be run from the root of the Git repository."
    exit 1
fi

MONO_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current Monorepo branch: $MONO_BRANCH"

# Detect uncommitted changes and stash
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Local changes detected. Stashing before push..."
    git stash push -m "Temp stash before PushAll"
    STASHED=true
else
    STASHED=false
fi

# List of modules and their remotes
declare -A MODULES
MODULES["APIGatewayService"]="git@github.com:max3tmk/APIGatewayService.git"
MODULES["AuthenticationService"]="git@github.com:max3tmk/AuthenticationService.git"
MODULES["Common"]="git@github.com:max3tmk/Common.git"
MODULES["ImageService"]="git@github.com:max3tmk/ImageService.git"
MODULES["frontend"]="git@github.com:max3tmk/frontend.git"

REVIEWER="AleksandrDInno"

for module in "${!MODULES[@]}"; do
    REMOTE_URL=${MODULES[$module]}
    echo "Processing module: $module"

    # Check if module folder exists
    if [ ! -d "$module" ]; then
        echo "Module folder $module not found, skipping..."
        continue
    fi

    if [ "$MONO_BRANCH" == "main" ]; then
        # Push main branch directly
        echo "Pushing $module directly to main branch (preserving history)..."
        git subtree push --prefix="$module" "$REMOTE_URL" main || echo "Nothing to push for $module"
    else
        # Feature branch: push to same branch in module and create PR
        echo "Pushing $module to branch $MONO_BRANCH and creating PR..."

        # Create temporary split branch
        TMP_BRANCH="temp_split_branch_$module"
        git branch -D "$TMP_BRANCH" 2>/dev/null || true
        git subtree split --prefix="$module" -b "$TMP_BRANCH"

        # Push to module remote branch
        git push -u "$REMOTE_URL" "$TMP_BRANCH:$MONO_BRANCH" || echo "Push failed for $module"

        # Create pull request using GitHub CLI
        PR_BODY=$(git log --format="%h %s" origin/main.."$TMP_BRANCH")
        set +e
        gh pr create --repo "$REMOTE_URL" --base main --head "$MONO_BRANCH" --title "$MONO_BRANCH" --body "$PR_BODY" --reviewer "$REVIEWER"
        if [ $? -ne 0 ]; then
            echo "WARNING: Could not assign reviewer $REVIEWER for $module. Please check manually."
        fi
        set -e

        # Delete temporary branch
        git branch -D "$TMP_BRANCH"
    fi
done

# Restore stashed changes if any
if [ "$STASHED" = true ]; then
    echo "Restoring stashed local changes..."
    git stash pop
fi

echo "PushAll completed successfully."