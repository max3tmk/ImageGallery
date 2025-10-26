#!/usr/bin/env bash
# PushAll.sh
# Push changes from Monorepo into individual module repositories
# Preserves commit history using git subtree
# For main branch: push directly without PR
# For feature branches: push to module branch and create PR
# Safe to run multiple times

set -e

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

# GitHub username to assign as reviewer
REVIEWER="AleksandrDInno"

for module in "${!MODULES[@]}"; do
    REMOTE_URL=${MODULES[$module]}
    echo "Processing module: $module"

    # Check if module folder exists
    if [ ! -d "$module" ]; then
        echo "Module folder $module not found, skipping..."
        continue
    fi

    cd "$module"

    if [ "$MONO_BRANCH" == "main" ]; then
        echo "Pushing $module directly to main branch (preserving history)..."
        git subtree push --prefix="$module" "$REMOTE_URL" main || echo "Nothing to push for $module"
    else
        echo "Pushing $module to branch $MONO_BRANCH and creating PR..."
        # Create subtree branch
        git subtree split --prefix="$module" -b temp_split_branch

        # Push using --force-with-lease for safety
        git push -u "$REMOTE_URL" temp_split_branch:"$MONO_BRANCH" --force-with-lease || {
            echo "Warning: push failed for $module. Check branch $MONO_BRANCH on remote."
        }

        # Create PR using GitHub CLI
        PR_BODY=$(git log --format="%h %s" origin/main..temp_split_branch)
        set +e
        gh pr create --repo "$REMOTE_URL" --base main --head "$MONO_BRANCH" --title "$MONO_BRANCH" --body "$PR_BODY" --reviewer "$REVIEWER"
        if [ $? -ne 0 ]; then
            echo "Warning: could not assign reviewer $REVIEWER for module $module. Please verify manually."
        fi
        set -e

        git branch -D temp_split_branch
    fi

    cd ..
done

# Restore stashed changes if any
if [ "$STASHED" = true ]; then
    echo "Restoring stashed local changes..."
    git stash pop
fi

echo "PushAll completed."