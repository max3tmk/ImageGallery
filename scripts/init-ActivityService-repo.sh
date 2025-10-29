#!/usr/bin/env bash
# Copy ActivityService to separate repository
# from feature/ActivityService-Back

set -e

MODULE="ActivityService"
SOURCE_REMOTE="origin"
SOURCE_BRANCH="feature/ActivityService-Back"
TARGET_REPO="git@github.com:max3tmk/ActivityService.git"

echo "Retrieving changes from remote mono repository..."
git fetch "$SOURCE_REMOTE" "$SOURCE_BRANCH"

SUBTREE_HASH=$(git rev-parse "$SOURCE_REMOTE/$SOURCE_BRANCH")
echo "module hash code $MODULE: $SUBTREE_HASH"

TMP_BRANCH="tmp_split_$MODULE"
git branch -D "$TMP_BRANCH" 2>/dev/null || true
git subtree split --prefix="$MODULE" "$SUBTREE_HASH" -b "$TMP_BRANCH"
echo "temporary branch $TMP_BRANCH created"

git push "$TARGET_REPO" "$TMP_BRANCH:refs/heads/$SOURCE_BRANCH"
echo "Changes from $SOURCE_BRANCH moved to $TARGET_REPO"

git branch -D "$TMP_BRANCH"
echo "Temporary branch $TMP_BRANCH deleted"

echo "Complete!"