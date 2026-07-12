#!/usr/bin/env bash
#
# Build universal (arm64 + x86_64) native binaries on macOS -> native/build/dist.
#
# Same shape as build-native-linux.sh; the macOS-specific bits are the universal
# builds:
#   - C++ (deps + tools): one build with -DCMAKE_OSX_ARCHITECTURES="arm64;x86_64"
#   - Rust: build each apple target, then lipo the exes into one fat binary
#
# Requirements: Xcode clang (>= the one that ships <format>), cmake, ninja, git,
# rust toolchain.
set -eu

cd "$(dirname "${BASH_SOURCE[0]}")/.."

: "${BUILD_TYPE:=Release}"
: "${OPENCV_VERSION:=4.12.0}"
: "${NLOPT_VERSION:=v2.7.1}"
: "${DIPLIB_VERSION:=3.6.0}"
: "${CXX:=c++}"
# Floor for the release: libc++ std::format needs macOS 13.3+ (the mac analogue
# of the glibc floor on Linux). Applied to every C++ build for a consistent min.
: "${MACOSX_DEPLOYMENT_TARGET:=13.3}"
export MACOSX_DEPLOYMENT_TARGET

ARCHS="arm64;x86_64"

BUILD_DIR="$PWD/native/build"
PREFIX="$BUILD_DIR/prefix"
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

# --- Rust: build both apple targets, lipo into universal binaries -------------
blank_lines
echo "---------------------------------------- Rust build (universal)"
rustup target add aarch64-apple-darwin x86_64-apple-darwin
cargo build --release --manifest-path native/rust/Cargo.toml --target-dir "$BUILD_DIR/rust" --target aarch64-apple-darwin
cargo build --release --manifest-path native/rust/Cargo.toml --target-dir "$BUILD_DIR/rust" --target x86_64-apple-darwin
mkdir -p native/build/dist
for exe in assessment frc_this f2i split; do
	lipo -create \
		"$BUILD_DIR/rust/aarch64-apple-darwin/release/$exe" \
		"$BUILD_DIR/rust/x86_64-apple-darwin/release/$exe" \
		-output "native/build/dist/$exe"
done

blank_lines
echo "---------------------------------------- C++ build (universal)"
# Build a pretty minimal OpenCV
OPENCV_SRC="$BUILD_DIR/opencv"
if [ ! -f "$PREFIX/lib/cmake/opencv4/OpenCVConfig.cmake" ]; then
	clone_repo https://github.com/opencv/opencv.git "$OPENCV_VERSION" "$OPENCV_SRC"
	blank_lines
	echo "-------------------- OpenCV"
	cmake -S "$OPENCV_SRC" -B "$OPENCV_SRC/build" -G Ninja \
		-DCMAKE_CXX_COMPILER="$CXX" \
		-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
		-DCMAKE_OSX_ARCHITECTURES="$ARCHS" \
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
		-DWITH_FFMPEG=OFF \
		-DWITH_GSTREAMER=OFF \
		-DWITH_AVFOUNDATION=OFF \
		-DWITH_QUARTZ=OFF \
		-DWITH_OPENCL=OFF \
		-DWITH_IPP=OFF \
		-DWITH_ITT=OFF \
		-DWITH_LAPACK=OFF \
		-DWITH_TBB=OFF \
		-DBUILD_JAVA=OFF \
		-DBUILD_opencv_python3=OFF \
		-DBUILD_opencv_apps=OFF \
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
		-DCMAKE_OSX_ARCHITECTURES="$ARCHS" \
		-DBUILD_SHARED_LIBS=OFF \
		-DCMAKE_INSTALL_PREFIX="$PREFIX"
	ninja -C "$NLOPT_SRC/build" install
else
	echo "==> Reusing NLopt in $PREFIX"
fi

# Just check out diplib: built via add_subdirectory by the tools CMake.
clone_repo https://github.com/DIPlib/diplib.git "$DIPLIB_VERSION" "$DIPLIB_SRC"

# Build hawkman and squirrel (universal)
blank_lines
echo "-------------------- hawkman/squirrel"
rm -rf "$BUILD_DIR/tools"
cmake -S native/cpp/tools -B "$BUILD_DIR/tools" -G Ninja \
	-DCMAKE_CXX_COMPILER="$CXX" \
	-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
	-DCMAKE_OSX_ARCHITECTURES="$ARCHS" \
	-DCMAKE_PREFIX_PATH="$PREFIX" \
	-DDIPLIB_DIR="$DIPLIB_SRC" \
	-DDIP_BUILD_JAVAIO=OFF \
	-DDIP_BUILD_DIPIMAGE=OFF \
	-DDIP_BUILD_DIPVIEWER=OFF \
	-DDIP_BUILD_PYDIP=OFF \
	-DDIP_SHARED_LIBRARY=OFF \
	-DDIP_ENABLE_DOCTEST=OFF \
	-DDIP_ENABLE_ICS=OFF \
	-DDIP_ENABLE_TIFF=OFF \
	-DDIP_ENABLE_JPEG=OFF \
	-DDIP_ENABLE_PNG=OFF \
	-DDIP_ENABLE_ZLIB=OFF

echo "==> Building C++ tools"
ninja -C "$BUILD_DIR/tools"

cp "$BUILD_DIR/tools"/{hawkman,squirrel} native/build/dist/
