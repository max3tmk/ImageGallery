#!/usr/local/bin/bash
# Pull updates from all module repositories into Monorepo branch
# Commit and push updates into remote Monorepo

set -e

MONO_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current Monorepo branch: $MONO_BRANCH"

declare -A MODULES
MODULES["APIGatewayService"]="git@github.com:max3tmk/APIGatewayService.git"
MODULES["AuthenticationService"]="git@github.com:max3tmk/AuthenticationService.git"
MODULES["Common"]="git@github.com:max3tmk/Common.git"
MODULES["ImageService"]="git@github.com:max3tmk/ImageService.git"
MODULES["frontend"]="git@github.com:max3tmk/frontend.git"
MODULES["ActivityService"]="git@github.com:max3tmk/ActivityService.git"

MONO_COMMIT_BODY="Pull updates from module repositories:\n"

for module in "${!MODULES[@]}"; do
    REMOTE_URL=${MODULES[$module]}
    echo "--------------------------------------------"
    echo "Pulling updates for module: $module"
    REMOTE_URL=${MODULES[$module]}

    # Try to fetch updates from remote branch
    set +e
    git fetch "$REMOTE_URL" "$MONO_BRANCH"
    FETCH_EXIT=$?
    set -e

    if [ $FETCH_EXIT -ne 0 ]; then
        echo "WARNING: Failed to fetch $MONO_BRANCH from $module. Skipping..."
        continue
    fi

    # Pull updates via subtree
    set +e
    git subtree pull --prefix="$module" "$REMOTE_URL" "$MONO_BRANCH" --squash
    SUBTREE_EXIT=$?
    set -e

    if [ $SUBTREE_EXIT -ne 0 ]; then
        echo "WARNING: Nothing to pull or subtree pull failed for $module."
    else
        echo "Subtree pull successful for $module."
    fi

    MODULE_COMMITS=$(git log --format="%h %s" HEAD@{1}..HEAD 2>/dev/null)
    if [ -n "$MODULE_COMMITS" ]; then
        MONO_COMMIT_BODY+="$module:\n$MODULE_COMMITS\n\n"
    fi
done

git add -A

if ! git diff --cached --quiet; then
    echo "Changes detected in monorepo. Committing..."
    git commit -m "Sync modules into Monorepo ($MONO_BRANCH)" -m "$MONO_COMMIT_BODY"
    set +e
    git push origin "$MONO_BRANCH"
    if [ $? -ne 0 ]; then
        echo "WARNING: Push to origin/$MONO_BRANCH failed. Please check manually."
    else
        echo "Push to origin/$MONO_BRANCH successful."
    fi
    set -e
else
    echo "No changes to commit in Monorepo."
fi

echo "PullAll completed."