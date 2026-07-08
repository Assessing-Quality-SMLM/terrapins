#!/usr/bin/env bash
#
# Build all TERRAPINS native binaries on Linux.
#
# Produces the six native executables:
#   Rust  (native/rust):        assessment, frc_this, f2i, split
#   C++   (native/cpp/tools):   hawkman, squirrel
# and collects them into "$OUTPUT_DIR" (default: native/dist/linux).
#
# The C++ dependencies are built from source following the hawkman build
# instructions (the C++ tools are vendored from the hawkman repo):
#   - OpenCV: static, TIFF-only (the plugin exchanges TIFFs with the exe, so
#             JPEG/PNG/etc are deliberately disabled), modules core,imgproc,
#             imgcodecs, installed into a local prefix.
#   - NLopt:  static, installed into the same prefix.
#   - DIPlib: not built standalone - the tools CMake builds it via
#             add_subdirectory(${DIPLIB_DIR}); we only fetch its source.
# OpenCV and NLopt are found via CMAKE_PREFIX_PATH; DIPlib via -DDIPLIB_DIR.
#
# Everything installs into native/.deps - no system packages, no sudo.
# Expected on PATH: a C++23 compiler (gcc >= 13 or clang >= 17), cmake, ninja,
# git and a Rust toolchain. Set CC/CXX to pick a specific compiler.
#
# Configuration (all overridable via environment):
#   BUILD_TYPE        CMake build type              (default: Release)
#   OPENCV_VERSION    OpenCV git tag                (default: 4.12.0)
#   NLOPT_VERSION     NLopt git tag                 (default: v2.7.1)
#   DIPLIB_VERSION    DIPlib git tag                (default: 3.6.0)
#   OUTPUT_DIR        where binaries are collected  (default: native/dist/linux)
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

BUILD_TYPE="${BUILD_TYPE:-Release}"
OPENCV_VERSION="${OPENCV_VERSION:-4.12.0}"
NLOPT_VERSION="${NLOPT_VERSION:-v2.7.1}"
DIPLIB_VERSION="${DIPLIB_VERSION:-3.6.0}"
OUTPUT_DIR="${OUTPUT_DIR:-$REPO_ROOT/native/dist/linux}"
CXX="${CXX:-c++}"

DEPS_DIR="$REPO_ROOT/native/.deps"
PREFIX="$DEPS_DIR/prefix"          # install prefix for OpenCV + NLopt
DIPLIB_SRC="$DEPS_DIR/diplib-$DIPLIB_VERSION"
CPP_BUILD_DIR="$REPO_ROOT/native/cpp/tools/build"
RUST_MANIFEST="$REPO_ROOT/native/rust/Cargo.toml"
RUST_TARGET_DIR="$REPO_ROOT/native/rust/target/release"


mkdir -p "$DEPS_DIR" "$PREFIX"

# Clone a repo once (shallow), skipping if already present.
clone_repo() {
	local url="$1" dest="$2"
	if [ -d "$dest/.git" ]; then
		echo "==> Reusing source: $dest"
	else
		echo "==> Fetching $(basename "$dest")"
		rm -rf "$dest"
		git clone --depth 1 "$url" "$dest"
	fi
}

# --- 1. Rust binaries --------------------------------------------------------
echo "==> Building Rust workspace (release)"
cargo build --release --manifest-path "$RUST_MANIFEST"

# --- 2. OpenCV (static, TIFF-only, core/imgproc/imgcodecs) -------------------
OPENCV_SRC="$DEPS_DIR/opencv-$OPENCV_VERSION"
if [ ! -f "$PREFIX/lib/cmake/opencv4/OpenCVConfig.cmake" ] && \
   [ ! -f "$PREFIX/lib64/cmake/opencv4/OpenCVConfig.cmake" ]; then
	clone_repo https://github.com/opencv/opencv.git "$OPENCV_SRC"
	echo "==> Building OpenCV $OPENCV_VERSION"
	cmake -S "$OPENCV_SRC" -B "$OPENCV_SRC/build" -G Ninja \
		-DCMAKE_CXX_COMPILER="$CXX" \
		-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
		-DBUILD_SHARED_LIBS=OFF \
		-DENABLE_PIC=ON \
		-DCMAKE_INSTALL_PREFIX="$PREFIX" \
		-DWITH_PNG=OFF \
		-DWITH_JPEG=OFF \
		-DWITH_WEBP=OFF \
		-DWITH_JASPER=OFF \
		-DWITH_OPENJPEG=OFF \
		-DWITH_OPENEXR=OFF \
		-DWITH_JPEGXL=OFF \
		-DBUILD_TIFF=ON \
		-DWITH_IMGCODEC_HDR=OFF \
		-DWITH_IMGCODEC_SUNRASTER=OFF \
		-DWITH_IMGCODEC_PXM=OFF \
		-DWITH_IMGCODEC_PFM=OFF \
		-DWITH_IMGCODEC_GIF=OFF \
		-DWITH_PROTOBUF=OFF \
		-DWITH_ADE=OFF \
		-DWITH_EIGEN=OFF \
		-DBUILD_LIST=core,imgproc,imgcodecs
	ninja -C "$OPENCV_SRC/build" install
else
	echo "==> Reusing OpenCV in $PREFIX"
fi

# --- 3. NLopt (static) -------------------------------------------------------
NLOPT_SRC="$DEPS_DIR/nlopt-$NLOPT_VERSION"
if [ ! -f "$PREFIX/lib/cmake/nlopt/NLoptConfig.cmake" ] && \
   [ ! -f "$PREFIX/lib64/cmake/nlopt/NLoptConfig.cmake" ]; then
	clone_repo https://github.com/stevengj/nlopt.git "$NLOPT_SRC"
	echo "==> Building NLopt $NLOPT_VERSION"
	cmake -S "$NLOPT_SRC" -B "$NLOPT_SRC/build" -G Ninja \
		-DCMAKE_CXX_COMPILER="$CXX" \
		-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
		-DBUILD_SHARED_LIBS=OFF \
		-DCMAKE_INSTALL_PREFIX="$PREFIX"
	ninja -C "$NLOPT_SRC/build" install
else
	echo "==> Reusing NLopt in $PREFIX"
fi

# --- 4. DIPlib source (built by the tools CMake via add_subdirectory) --------
clone_repo https://github.com/DIPlib/diplib.git "$DIPLIB_SRC"

# --- 5. C++ binaries (hawkman, squirrel) -------------------------------------
echo "==> Configuring C++ tools (CMake, $BUILD_TYPE)"
rm -rf "$CPP_BUILD_DIR"
cmake -S "$REPO_ROOT/native/cpp/tools" -B "$CPP_BUILD_DIR" -G Ninja \
	-DCMAKE_CXX_COMPILER="$CXX" \
	-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
	-DCMAKE_PREFIX_PATH="$PREFIX" \
	-DDIPLIB_DIR="$DIPLIB_SRC" \
	-DDIP_BUILD_JAVAIO=OFF \
	-DDIP_BUILD_DIPIMAGE=OFF \
	-DDIP_BUILD_DIPVIEWER=OFF \
	-DDIP_BUILD_PYDIP=OFF \
	-DDIP_SHARED_LIBRARY=OFF \
	-DDIP_ENABLE_DOCTEST=OFF

echo "==> Building C++ tools"
ninja -C "$CPP_BUILD_DIR"

# --- 6. Collect binaries -----------------------------------------------------
echo "==> Collecting binaries into $OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Rust CLIs
cp "$RUST_TARGET_DIR/assessment" "$OUTPUT_DIR/"
cp "$RUST_TARGET_DIR/frc_this" "$OUTPUT_DIR/"
cp "$RUST_TARGET_DIR/f2i" "$OUTPUT_DIR/"
cp "$RUST_TARGET_DIR/split" "$OUTPUT_DIR/"
# C++ tools
cp "$CPP_BUILD_DIR/hawkman" "$OUTPUT_DIR/"
cp "$CPP_BUILD_DIR/squirrel" "$OUTPUT_DIR/"

echo "==> Done. Native Linux binaries are in: $OUTPUT_DIR"
# To assemble the ImageJ jar, copy these into
# imagej/src/main/resources/nix/bin (see BUILD.md) before running mvn install.
