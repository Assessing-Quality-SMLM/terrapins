#!/usr/bin/env bash
#
# Build the TERRAPINS ImageJ plugin jar for the CURRENT platform (local dev).
#   1. build the native binaries for this platform
#   2. stage them into the plugin's resources
#   3. build the fat jar with Maven
#
# Single-platform: the jar contains only this platform's binaries. CI
# (.github/workflows/build.yml) builds the multi-platform release jar.
#
# Requirements: everything build-native-<platform>.sh needs, plus a JDK and Maven.
set -eu

cd "$(dirname "${BASH_SOURCE[0]}")/.."

[ "$(uname -s)" = Linux ] || { echo "only Linux is supported for now" >&2; exit 1; }

# 1. native binaries -> native/build/dist
scripts/build-native-linux.sh

# 2. stage them where ffi.java loads them from at runtime (nix/bin in the jar)
res="imagej/src/main/resources/nix/bin"
rm -rf "$res"
mkdir -p "$res"
cp native/build/dist/* "$res/"

# 3. fat jar
mvn -Denforcer.skip -Dmaven.test.skip=true -f imagej/pom.xml install

echo "==> plugin jar:"
ls imagej/target/*-jar-with-dependencies.jar
