#!/usr/bin/env bash

set -eo pipefail

# cd to project root
cd "$(dirname "$0")/.."

[ "$DETEKT_CHANGED_FILES_ONLY" == "" ] && DETEKT_CHANGED_FILES_ONLY="true"
DETEKT_STDOUT_FOLDER="build/reports/detekt"
DETEKT_STDOUT_FILE="$DETEKT_STDOUT_FOLDER/detekt.txt"
DETEKT_STATUS=-1

RED='\e[0;31m'
GREEN='\e[0;32m'
CYAN='\e[0;36m'
NC='\e[0m' # No Color

mkdir -p "$DETEKT_STDOUT_FOLDER"

# Get a list of all files that are staged for a git commit (--diff-filter=d to ignore deleted files)
GIT_STAGED_FILES=$(git diff --cached --name-only --diff-filter=d | (grep "\\.kt" || true))

reAddChangedFiles(){
    # Changed files get removed from the git index, so we need to stage them again.
    if [ "$GIT_STAGED_FILES" != ""  ]; then
        while read -r file; do
            git add "$file"
        done <<< "$GIT_STAGED_FILES"
    fi
}

runDetekt(){
    set +e
    ./gradlew -PdetektFiles="$GIT_STAGED_FILES" --rerun-tasks detekt > "$DETEKT_STDOUT_FILE" 2>&1
    DETEKT_STATUS=$?
    set -e
    reAddChangedFiles
}

if [ ${DETEKT_CHANGED_FILES_ONLY} != "true" ]; then
    printf "${CYAN}Executing Detekt on all files... "
else
    printf "${CYAN}Executing Detekt on changed files (Pass env var 'DETEKT_CHANGED_FILES_ONLY=false' to check all files)${NC}\n"
    if [ "$GIT_STAGED_FILES" == "" ]; then
        printf "${GREEN}No changes detekted.${NC}\n"
        exit 0
    else
        NUM_FILES=$(wc -l <<< "$GIT_STAGED_FILES")
        NUM_FILES="${NUM_FILES// }" # remove whitespaces
        printf "${CYAN}Executing Detekt on $NUM_FILES changed files... "
    fi
fi

runDetekt

if [ ${DETEKT_STATUS} -ne 0 ]; then
    # Run a second time if the first time fails.
    # The first time might fix all formatting errors, but still returns a non-zero exit code.
    # If it was able to fix everything on its own, the second iteration will return a zero exit code.
    printf "${RED}Detekt Failed!$NC\n${CYAN}Retry... "
    runDetekt
fi

if [ ${DETEKT_STATUS} -eq 0 ]; then
    printf "${GREEN}Detekt successful!$NC\n"
    exit 0
else
    printf "${RED}Detekt failed! Fix the following errors before committing:$NC\n"
    cat "$DETEKT_STDOUT_FILE" | grep " - " | grep -v "Ruleset"
    exit 1
fi
