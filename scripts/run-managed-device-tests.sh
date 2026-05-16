#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

exec "${repo_root}/scripts/gradlew21.sh" \
  pixel2Api36DebugAndroidTest \
  --no-parallel \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect \
  "$@"
