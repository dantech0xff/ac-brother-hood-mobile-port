#!/usr/bin/env bash
# Bootstrap a Linux dev box (or a cloud session) for the rewrite/ gates:
#   JDK 17+  → Gradle, the port tests and the verifier's javap
#   Android SDK (platform-tools, platforms;android-36, build-tools;35.0.0)
#            → :android:assembleDebug
# Idempotent: every step checks first and only installs what is missing.
# It never touches the original JAR beyond what the static verifier reads.
#
# Usage: rewrite/tools/bootstrap-dev-env.sh [--no-install]
#   --no-install   only report what is missing (exit 1 if anything is)
set -euo pipefail

SDK_PACKAGES=("platform-tools" "platforms;android-36" "build-tools;35.0.0")
CMDLINE_TOOLS_ZIP="commandlinetools-linux-13114758_latest.zip"
REWRITE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

install=1
[[ "${1:-}" == "--no-install" ]] && install=0
missing=0

say() { printf '[bootstrap] %s\n' "$*"; }

as_root() {
    if [[ $(id -u) -eq 0 ]]; then "$@"
    elif command -v sudo >/dev/null 2>&1; then sudo "$@"
    else say "need root for: $*"; return 1
    fi
}

# --- JDK -------------------------------------------------------------------
java_major() {
    local v
    v=$("$1" -version 2>&1 | awk -F'"' '/version/ {print $2; exit}')
    v=${v#1.}
    echo "${v%%.*}"
}

java_bin=""
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then java_bin="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then java_bin=$(command -v java)
fi
if [[ -n "$java_bin" && $(java_major "$java_bin") -ge 17 ]]; then
    say "JDK ok: $("$java_bin" -version 2>&1 | grep -m1 ' version ')"
else
    missing=1
    if [[ $install -eq 1 ]] && command -v apt-get >/dev/null 2>&1; then
        say "installing openjdk-17-jdk-headless"
        as_root apt-get update -q
        as_root apt-get install -y -q openjdk-17-jdk-headless
    else
        say "JDK 17+ missing (install a Temurin/OpenJDK 17 and set JAVA_HOME)"
    fi
fi
command -v javap >/dev/null 2>&1 || say "warning: javap not on PATH (the verifier needs it)"

# --- Android SDK -----------------------------------------------------------
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$sdk" ]]; then
    for d in "$HOME/Android/Sdk" "$HOME/android-sdk" /opt/android-sdk; do
        [[ -d "$d" ]] && { sdk="$d"; break; }
    done
fi
sdk="${sdk:-$HOME/Android/Sdk}"

sdkmanager="$sdk/cmdline-tools/latest/bin/sdkmanager"
if [[ ! -x "$sdkmanager" ]]; then
    missing=1
    if [[ $install -eq 1 ]]; then
        say "installing Android command-line tools into $sdk"
        tmp=$(mktemp -d)
        curl -fsSL -o "$tmp/tools.zip" "https://dl.google.com/android/repository/$CMDLINE_TOOLS_ZIP"
        mkdir -p "$sdk/cmdline-tools"
        unzip -q "$tmp/tools.zip" -d "$tmp"
        rm -rf "$sdk/cmdline-tools/latest"
        mv "$tmp/cmdline-tools" "$sdk/cmdline-tools/latest"
        rm -rf "$tmp"
    else
        say "Android cmdline-tools missing under $sdk"
    fi
fi

need=()
for p in "${SDK_PACKAGES[@]}"; do
    [[ -d "$sdk/${p//;//}" ]] || need+=("$p")
done
if [[ ${#need[@]} -eq 0 ]]; then
    say "Android SDK ok: $sdk"
else
    missing=1
    if [[ $install -eq 1 && -x "$sdkmanager" ]]; then
        say "installing SDK packages: ${need[*]}"
        yes | "$sdkmanager" --sdk_root="$sdk" --licenses >/dev/null || true
        "$sdkmanager" --sdk_root="$sdk" "${need[@]}"
    else
        say "SDK packages missing: ${need[*]}"
    fi
fi

# Gradle finds the SDK through local.properties (git-ignored) or ANDROID_HOME.
props="$REWRITE_DIR/local.properties"
if [[ -d "$sdk" ]] && ! grep -q '^sdk.dir=' "$props" 2>/dev/null; then
    if [[ $install -eq 1 ]]; then
        echo "sdk.dir=$sdk" >> "$props"
        say "wrote sdk.dir to $props"
    else
        say "rewrite/local.properties has no sdk.dir (or export ANDROID_HOME=$sdk)"
    fi
fi

if [[ $install -eq 0 && $missing -eq 1 ]]; then exit 1; fi
say "done — gates: see AGENTS.md (verifier, unittest, :core:test :gdx:test :android:assembleDebug)"
