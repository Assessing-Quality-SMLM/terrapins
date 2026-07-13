#!/usr/bin/env bash
#
# Build all TERRAPINS native binaries (Linux and macOS) -> native/build/dist
#   Rust:    assessment, frc_this, f2i, split
#   C++:     hawkman, squirrel
#
# The C++ dependencies are built from source as static libs (where applicable)
# and in the most minimal way possible (i.e. TIFF loading, little else optional)
#
# macOS builds the host arch only (arm64 on Apple Silicon) - FFTW can't be
# cross-compiled, so a universal build would need a second Intel runner + lipo.
#
# Requirements:
# C++23 (override compiler with CXX=)
# cmake
# ninja
# git
# rust toolchain
set -eu

cd "$(dirname "${BASH_SOURCE[0]}")/.."

BUILD_TYPE=Release
OPENCV_VERSION=4.12.0
NLOPT_VERSION=v2.7.1
DIPLIB_VERSION=3.6.0
: "${CXX:=c++}"

# macOS: libc++ std::format needs a 13.3+ deployment target (the release floor).
if [ "$(uname)" = Darwin ]; then
	export MACOSX_DEPLOYMENT_TARGET="${MACOSX_DEPLOYMENT_TARGET:-13.3}"
fi

# absolute: DIPLIB_SRC feeds CMake's add_subdirectory(${DIPLIB_DIR}), which resolves
# a relative path against the CMakeLists dir (native/cpp/lib), not our cwd
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

blank_lines
echo "---------------------------------------- Rust build"
cargo build --release --manifest-path native/rust/Cargo.toml --target-dir "$BUILD_DIR/rust"



blank_lines
echo "---------------------------------------- C++ build"
# Build a pretty minimal OpenCV. The platform-specific WITH_* below are a union
# of Linux + macOS options; CMake just warns about the ones that don't apply.
OPENCV_SRC="$BUILD_DIR/opencv"
if [ ! -f "$PREFIX/lib/cmake/opencv4/OpenCVConfig.cmake" ]; then
	clone_repo https://github.com/opencv/opencv.git "$OPENCV_VERSION" "$OPENCV_SRC"
	blank_lines
	echo "-------------------- OpenCV"
	cmake -S "$OPENCV_SRC" -B "$OPENCV_SRC/build" -G Ninja \
		-DCMAKE_CXX_COMPILER="$CXX" \
		-DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
		-DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
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
		-DWITH_V4L=OFF \
		-DWITH_1394=OFF \
		-DWITH_GPHOTO2=OFF \
		-DWITH_GTK=OFF \
		-DWITH_QT=OFF \
		-DWITH_OPENGL=OFF \
		-DWITH_AVFOUNDATION=OFF \
		-DWITH_QUARTZ=OFF \
		-DWITH_OPENCL=OFF \
		-DWITH_IPP=OFF \
		-DWITH_ITT=OFF \
		-DWITH_VA=OFF \
		-DWITH_VA_INTEL=OFF \
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
		-DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
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
	-DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
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

# Put the exes in one dir
mkdir -p native/build/dist/
cp "$BUILD_DIR/rust/release"/{assessment,frc_this,f2i,split} native/build/dist/
cp "$BUILD_DIR/tools"/{hawkman,squirrel} native/build/dist/
