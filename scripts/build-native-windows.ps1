# Build native binaries on Windows (MSVC) -> native\build\dist\*.exe
#
# ⚠ UNTESTED SCAFFOLD - needs iterating on a real windows-latest runner, same as
# the Linux ninja issue. Known risk points are flagged with TODO below.
#
# Design (differs from the *nix scripts):
#   - MSVC via the Visual Studio generator (auto-locates the compiler; no vcvars)
#   - static CRT (/MT) so the exes need no VC++ redistributable on the target
#   - Rust uses the default x86_64-pc-windows-msvc toolchain
#
# Run with PowerShell 7 (pwsh) so native-command failures abort the script.
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true   # throw on non-zero exit (pwsh 7.3+)

Set-Location (Join-Path $PSScriptRoot '..')

$BuildType     = 'Release'
$OpenCVVersion = '4.12.0'
$NloptVersion  = 'v2.7.1'
$DiplibVersion = '3.6.0'

$BuildDir  = Join-Path (Get-Location) 'native\build'
$Prefix    = Join-Path $BuildDir 'prefix'
$DiplibSrc = Join-Path $BuildDir 'diplib'
$Dist      = Join-Path $BuildDir 'dist'
New-Item -ItemType Directory -Force -Path $BuildDir, $Prefix, $Dist | Out-Null

# cmake needs forward slashes in -D path args (esp. DIPLIB_DIR -> add_subdirectory)
function ToCMakePath($p) { return $p.Replace('\', '/') }

# MSVC, x64, static CRT. CMP0091=NEW makes CMAKE_MSVC_RUNTIME_LIBRARY take effect.
$Gen = @(
    '-G', 'Visual Studio 17 2022', '-A', 'x64',
    '-DCMAKE_POLICY_DEFAULT_CMP0091=NEW',
    '-DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreaded'
)

function Clone-Repo($url, $tag, $dest) {
    Write-Host "==> Fetching $(Split-Path $dest -Leaf) ($tag)"
    git init $dest
    git -C $dest fetch --depth 1 $url "+refs/tags/${tag}:refs/tags/${tag}"
    git -C $dest checkout $tag
}

# --- Rust binaries -----------------------------------------------------------
Write-Host '==> Rust build'
cargo build --release --manifest-path native\rust\Cargo.toml --target-dir "$BuildDir\rust"
# TODO(fftw): frc_this depends on FFTW. The fftw-src crate builds FFTW from
# source and may not work under MSVC. The original plugin shipped a bundled
# fftw3.dll on Windows (see ffi.java's windows-only DLL extraction). If cargo
# fails here on fftw, provide a prebuilt FFTW (point the fftw crate at it via
# FFTW3_DIR / the `system` feature) and Copy-Item fftw3.dll into $Dist, and add
# it to imagej/src/main/resources/windows/lib in the packaging step.
foreach ($exe in 'assessment', 'frc_this', 'f2i', 'split') {
    Copy-Item "$BuildDir\rust\release\$exe.exe" $Dist
}

# --- OpenCV (static, TIFF-only) ----------------------------------------------
# No reuse-guard here: the Windows OpenCV install layout differs from *nix, so we
# just rebuild (CI is fresh anyway). TODO: add a guard once the path is known.
$OpenCVSrc = Join-Path $BuildDir 'opencv'
Clone-Repo 'https://github.com/opencv/opencv.git' $OpenCVVersion $OpenCVSrc
Write-Host '==> Building OpenCV'
cmake -S $OpenCVSrc -B "$OpenCVSrc\build" @Gen `
    "-DCMAKE_INSTALL_PREFIX=$(ToCMakePath $Prefix)" `
    -DBUILD_SHARED_LIBS=OFF `
    -DWITH_PNG=OFF -DWITH_JPEG=OFF -DWITH_WEBP=OFF -DWITH_JASPER=OFF `
    -DWITH_OPENJPEG=OFF -DWITH_OPENEXR=OFF -DWITH_JPEGXL=OFF `
    -DBUILD_TIFF=ON `
    -DWITH_IMGCODEC_HDR=OFF -DWITH_IMGCODEC_SUNRASTER=OFF -DWITH_IMGCODEC_PXM=OFF `
    -DWITH_IMGCODEC_PFM=OFF -DWITH_IMGCODEC_GIF=OFF `
    -DWITH_PROTOBUF=OFF -DWITH_ADE=OFF -DWITH_EIGEN=OFF `
    -DWITH_FFMPEG=OFF -DWITH_MSMF=OFF -DWITH_DSHOW=OFF -DWITH_DIRECTX=OFF `
    -DWITH_WIN32UI=OFF -DWITH_OPENCL=OFF -DWITH_IPP=OFF -DWITH_ITT=OFF `
    -DWITH_LAPACK=OFF -DWITH_TBB=OFF `
    -DBUILD_JAVA=OFF -DBUILD_opencv_python3=OFF -DBUILD_opencv_apps=OFF `
    -DBUILD_LIST=core,imgproc,imgcodecs
cmake --build "$OpenCVSrc\build" --config $BuildType
cmake --install "$OpenCVSrc\build" --config $BuildType

# --- NLopt (static) ----------------------------------------------------------
$NloptSrc = Join-Path $BuildDir 'nlopt'
Clone-Repo 'https://github.com/stevengj/nlopt.git' $NloptVersion $NloptSrc
Write-Host '==> Building NLopt'
cmake -S $NloptSrc -B "$NloptSrc\build" @Gen `
    "-DCMAKE_INSTALL_PREFIX=$(ToCMakePath $Prefix)" `
    -DBUILD_SHARED_LIBS=OFF
cmake --build "$NloptSrc\build" --config $BuildType
cmake --install "$NloptSrc\build" --config $BuildType

# --- DIPlib source (built by the tools CMake via add_subdirectory) -----------
Clone-Repo 'https://github.com/DIPlib/diplib.git' $DiplibVersion $DiplibSrc

# --- C++ tools (hawkman, squirrel) -------------------------------------------
Write-Host '==> Building hawkman/squirrel'
Remove-Item -Recurse -Force "$BuildDir\tools" -ErrorAction SilentlyContinue
cmake -S native\cpp\tools -B "$BuildDir\tools" @Gen `
    "-DCMAKE_PREFIX_PATH=$(ToCMakePath $Prefix)" `
    "-DDIPLIB_DIR=$(ToCMakePath $DiplibSrc)" `
    -DDIP_BUILD_JAVAIO=OFF -DDIP_BUILD_DIPIMAGE=OFF -DDIP_BUILD_DIPVIEWER=OFF `
    -DDIP_BUILD_PYDIP=OFF -DDIP_SHARED_LIBRARY=OFF -DDIP_ENABLE_DOCTEST=OFF `
    -DDIP_ENABLE_ICS=OFF -DDIP_ENABLE_TIFF=OFF -DDIP_ENABLE_JPEG=OFF `
    -DDIP_ENABLE_PNG=OFF -DDIP_ENABLE_ZLIB=OFF
cmake --build "$BuildDir\tools" --config $BuildType
# VS generator puts exes in a per-config subdir
Copy-Item "$BuildDir\tools\$BuildType\hawkman.exe" $Dist
Copy-Item "$BuildDir\tools\$BuildType\squirrel.exe" $Dist

Write-Host "==> binaries in $Dist"
Get-ChildItem $Dist
