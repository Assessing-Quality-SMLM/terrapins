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

# platform -> (imagej resource subdir, native build script). Windows is TODO.
case "$(uname -s)" in
	Linux)  sub=nix ; native=scripts/build-native-linux.sh ;;
	Darwin) sub=mac ; native=scripts/build-native-macos.sh ;;
	*) echo "only Linux and macOS are supported (Windows: TODO)" >&2; exit 1 ;;
esac

# 1. native binaries -> native/build/dist
"$native"

# 2. stage them where ffi.java loads them from at runtime (<os>/bin in the jar)
res="imagej/src/main/resources/$sub/bin"
rm -rf "$res"
mkdir -p "$res"
cp native/build/dist/* "$res/"

# 3. fat jar
mvn -Denforcer.skip -Dmaven.test.skip=true -f imagej/pom.xml install

echo "==> plugin jar:"
ls imagej/target/*-jar-with-dependencies.jar
