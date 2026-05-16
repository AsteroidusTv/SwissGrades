#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  current_major="$("${JAVA_HOME}/bin/java" -version 2>&1 | awk -F '[\".]' '/version/ {print $2; exit}')"
  if [[ "${current_major}" == "21" ]]; then
    exec "${repo_root}/gradlew" "$@"
  fi
fi

preferred_jdk="${HOME}/.local/jdks/amazon-corretto-21.0.11.10.1-linux-x64"
if [[ -x "${preferred_jdk}/bin/java" ]]; then
  export JAVA_HOME="${preferred_jdk}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  exec "${repo_root}/gradlew" "$@"
fi

for candidate in "${HOME}"/.local/jdks/*; do
  if [[ -x "${candidate}/bin/java" ]]; then
    major="$("${candidate}/bin/java" -version 2>&1 | awk -F '[\".]' '/version/ {print $2; exit}')"
    if [[ "${major}" == "21" ]]; then
      export JAVA_HOME="${candidate}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
      exec "${repo_root}/gradlew" "$@"
    fi
  fi
done

echo "No JDK 21 installation was found." >&2
echo "Install one under ~/.local/jdks or export JAVA_HOME to a JDK 21 path." >&2
exit 1
