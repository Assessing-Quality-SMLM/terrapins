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
# Everything installs into native/build - no system packages, no sudo.
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
set -eu

cd "$(dirname "${BASH_SOURCE[0]}")/.."

: "${BUILD_TYPE:=Release}"
: "${OPENCV_VERSION:=4.12.0}"
: "${NLOPT_VERSION:=v2.7.1}"
: "${DIPLIB_VERSION:=3.6.0}"
: "${OUTPUT_DIR:=native/dist/linux}"
: "${CXX:=c++}"

# absolute: DIPLIB_SRC feeds CMake's add_subdirectory(${DIPLIB_DIR}), which resolves
# a relative path against the CMakeLists dir (native/cpp/lib), not our cwd
BUILD_DIR="$PWD/native/build"
PREFIX="$BUILD_DIR/prefix"          # install prefix for OpenCV + NLopt
DIPLIB_SRC="$BUILD_DIR/diplib"


mkdir -p "$BUILD_DIR" "$PREFIX"

clone_repo() {
	local url="$1" tag="$2" dest="$3"
	echo -e '\n\n----------' "Fetching $(basename "$dest") ($tag)"
	git init "$dest" # This idempotent
	git -C "$dest" fetch --depth 1 "$url" "+refs/tags/$tag:refs/tags/$tag"
	git -C "$dest" checkout "$tag"
}

blank_lines() {
	yes '' | head -10
}

blank_lines
echo "---------------------------------------- Rust build"
cargo build --release --manifest-path native/rust/Cargo.toml --target-dir "$BUILD_DIR/rust"



blank_lines
echo "---------------------------------------- C++ build"
# Build a pretty minimal OpenCV
OPENCV_SRC="$BUILD_DIR/opencv"
if [ ! -f "$PREFIX/lib/cmake/opencv4/OpenCVConfig.cmake" ]; then
	clone_repo https://github.com/opencv/opencv.git "$OPENCV_VERSION" "$OPENCV_SRC"
	blank_lines
	echo "-------------------- OpenCV"
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

# NLOPT
NLOPT_SRC="$BUILD_DIR/nlopt"
if [ ! -f "$PREFIX/lib/cmake/nlopt/NLoptConfig.cmake" ]; then
	clone_repo https://github.com/stevengj/nlopt.git "$NLOPT_VERSION" "$NLOPT_SRC"
	blank_lines
	echo "-------------------- NLOPT"
	cmake -S "$NLOPT_SRC" -B "$NLOPT_SRC/build" -G Ninja \
		-DCMAKE_CXX_COMPILER="$CXX" \
		-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
		-DBUILD_SHARED_LIBS=OFF \
		-DCMAKE_INSTALL_PREFIX="$PREFIX"
	ninja -C "$NLOPT_SRC/build" install
else
	echo "==> Reusing NLopt in $PREFIX"
fi

# Just check out diplib: current instructions are to include as a subdirectory
# rather than a library. Not sure why.
clone_repo https://github.com/DIPlib/diplib.git "$DIPLIB_VERSION" "$DIPLIB_SRC"

# Build hawkman and squirrel
blank_lines
echo "-------------------- hawkman/squirrel"
rm -rf "$BUILD_DIR/tools"
cmake -S native/cpp/tools -B "$BUILD_DIR/tools" -G Ninja \
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
ninja -C "$BUILD_DIR/tools"

# Put the exes in one dir
mkdir -p "$OUTPUT_DIR"
cp "$BUILD_DIR/rust/release"/{assessment,frc_this,f2i,split} "$OUTPUT_DIR/"
cp "$BUILD_DIR/tools"/{hawkman,squirrel} "$OUTPUT_DIR/"
