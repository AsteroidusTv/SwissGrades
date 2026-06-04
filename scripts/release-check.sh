#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${repo_root}"

git diff --check

"${repo_root}/gradlew" \
  :app:lintRelease \
  :app:testReleaseUnitTest \
  :app:bundleRelease \
  --no-parallel

required_artifacts=(
  "app/build/outputs/bundle/release/app-release.aab"
  "app/build/outputs/mapping/release/mapping.txt"
  "app/build/outputs/native-debug-symbols/release/native-debug-symbols.zip"
)

for artifact in "${required_artifacts[@]}"; do
  if [[ ! -s "${artifact}" ]]; then
    echo "Missing required release artifact: ${artifact}" >&2
    exit 1
  fi
done

echo "Release check passed."
