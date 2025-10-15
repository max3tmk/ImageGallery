#!/usr/local/bin/bash
# Pull updates from all module repositories into Monorepo branch
# Commit and push updates into remote Monorepo

set -e

# Current branch in Monorepo
MONO_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current Monorepo branch: $MONO_BRANCH"

# List of modules
declare -A MODULES
MODULES["APIGatewayService"]="git@github.com:max3tmk/APIGatewayService.git"
MODULES["AuthenticationService"]="git@github.com:max3tmk/AuthenticationService.git"
MODULES["Common"]="git@github.com:max3tmk/Common.git"
MODULES["ImageService"]="git@github.com:max3tmk/ImageService.git"
MODULES["frontend"]="git@github.com:max3tmk/frontend.git"

MONO_COMMIT_BODY="Pull updates from module repositories:\n"

for module in "${!MODULES[@]}"; do
    echo "Pulling updates for module: $module"
    REMOTE_URL=${MODULES[$module]}

    cd "$module"

    git fetch "$REMOTE_URL"

    if git ls-remote --exit-code --heads "$REMOTE_URL" "$MONO_BRANCH" > /dev/null; then
        git checkout -B "$MONO_BRANCH" "$REMOTE_URL/$MONO_BRANCH"
        git pull "$REMOTE_URL" "$MONO_BRANCH"
    else
        git checkout -B "$MONO_BRANCH"
        git pull "$REMOTE_URL" main
    fi

    MODULE_COMMITS=$(git log --format="%h %s" HEAD@{1}..HEAD)
    if [ -n "$MODULE_COMMITS" ]; then
        MONO_COMMIT_BODY+="$module:\n$MODULE_COMMITS\n\n"
    fi

    cd ..
done

git add -A

if ! git diff --cached --quiet; then
    git commit -m "Sync modules into Monorepo ($MONO_BRANCH)" -m "$MONO_COMMIT_BODY"
    git push origin "$MONO_BRANCH"
else
    echo "No changes to commit in Monorepo"
fi

echo "PullAll completed."