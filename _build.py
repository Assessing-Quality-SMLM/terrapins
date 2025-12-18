from dev_ops import cargo, maven, hatch, gh, fs, utils

from os import path
import shutil
import sys


DEFAULT_ARTIFACT_NAME = "artifacts"
DEFAULT_ARTIFACT_FILE = "artifacts.zip"


def _merge(resources_dir: str, download_folder: str, repo: str, tag: str) -> bool:
    artifact_path = path.join(".", download_folder, DEFAULT_ARTIFACT_NAME)
    if not fs.create_directory(download_folder):
        return False
    if not gh.gh_merge_artifacts_into(repo, tag, download_folder):
        return False
    if not fs.merge_into(resources_dir, artifact_path):
        return False
    fs.remove_file(DEFAULT_ARTIFACT_FILE)
    return True


def _merge_split(resources_dir: str) -> bool:
    splitter_artifacts = "splitter_artifacts"
    splitter_repo = "https://github.com/Assessing-Quality-SMLM/localisations/"
    splitter_release = "split_0.3.0"
    return _merge(resources_dir, splitter_artifacts, splitter_repo, splitter_release)


def _merge_renderer(resources_dir: str) -> bool:
    renderer_artifacts = "renderer_artifacts"
    renderer_repo = "https://github.com/Assessing-Quality-SMLM/renderer/"
    renderer_release = "f2i_0.5.0"
    return _merge(resources_dir, renderer_artifacts, renderer_repo, renderer_release)


def _merge_frc(resources_dir: str) -> bool:
    frc_artifacts = "frc_artifacts"
    frc_repo = "https://github.com/Assessing-Quality-SMLM/frc/"
    frc_release = "frc_this_0.8.0"
    return _merge(resources_dir, frc_artifacts, frc_repo, frc_release)


def _merge_hawkman_and_squirrel(resources_dir: str) -> bool:
    artifacts = "hawkman_and_squirrel_artifacts"
    repo = "https://github.com/Assessing-Quality-SMLM/hawkman/"
    release = "hawkman_and_squirrel_0.14.0"
    return _merge(resources_dir, artifacts, repo, release)


def _merge_assessment(resources_dir: str) -> bool:
    artifacts = "assessment_artifacts"
    repo = "https://github.com/Assessing-Quality-SMLM/assessment/"
    release = "assessment_0.24.0"
    return _merge(resources_dir, artifacts, repo, release)


def _merge_jhawk(resources_dir: str) -> bool:
    jhawk_artifacts = "jhawk"
    jhawk_repo = "https://github.com/Assessing-Quality-SMLM/terrapins/"
    jhawk_release = "jhawk_0.5.0"
    return _merge(resources_dir, jhawk_artifacts, jhawk_repo, jhawk_release)


def _merge_hawk_ffi(resources_dir: str) -> bool:
    hawk_ffi_artifacts = "hawk_ffi"
    hawk_ffi_repo = "https://github.com/Assessing-Quality-SMLM/hawk/"
    hawk_ffi_release = "hawk_ffi_0.1.0"
    return _merge(resources_dir, hawk_ffi_artifacts, hawk_ffi_repo, hawk_ffi_release)


def _gather_tools_into(directory: str) -> bool:
    if not _merge_split(directory):
        return False
    if not _merge_renderer(directory):
        return False
    if not _merge_frc(directory):
        return False
    if not _merge_hawkman_and_squirrel(directory):
        return False
    if not _merge_assessment(directory):
        return False
    
    windows_blacklist = [
        "libfftw3-3.zip", 
        "opencv_videoio_msmf4100_64d.dll", 
        "opencv_world4100.pdb", 
        "opencv_world4100d.dll",
        "opencv_world4100d.pdb"
    ]
    
    nix_blacklist = [
        "libopencv_core.so",
        "libopencv_core.so.4.11.0",
        # "libopencv_core.so.411",

        "libopencv_imgcodecs.so",
        "libopencv_imgcodecs.so.4.11.0",
        # "libopencv_imgcodecs.so.411",

        "libopencv_imgproc.so",
        "libopencv_imgproc.so.4.11.0",
        # "libopencv_imgproc.so.411",

        "libopencv_calib3d.so",
        "libopencv_calib3d.so.4.11.0",
        "libopencv_calib3d.so.411",

        "libopencv_dnn.so",
        "libopencv_dnn.so.4.11.0",
        "libopencv_dnn.so.411",

        "libopencv_features2d.so",
        "libopencv_features2d.so.4.11.0",
        "libopencv_features2d.so.411",

        "libopencv_flann.so",
        "libopencv_flann.so.4.11.0",
        "libopencv_flann.so.411",

        "libopencv_gapi.so",
        "libopencv_gapi.so.4.11.0",
        "libopencv_gapi.so.411",

        "libopencv_highgui.so",
        "libopencv_highgui.so.4.11.0",
        "libopencv_highgui.so.411",

        "libopencv_java4110.so",

        "libopencv_ml.so",
        "libopencv_ml.so.4.11.0",
        "libopencv_ml.so.411",

        "libopencv_objdetect.so",
        "libopencv_objdetect.so.4.11.0",
        "libopencv_objdetect.so.411",

        "libopencv_photo.so",
        "libopencv_photo.so.4.11.0",
        "libopencv_photo.so.411",

        "libopencv_stitching.so",
        "libopencv_stitching.so.4.11.0",
        "libopencv_stitching.so.411",

        "libopencv_ts.a",

        "libopencv_video.so",
        "libopencv_video.so.4.11.0",
        "libopencv_video.so.411",

        "libopencv_videoio.so",
        "libopencv_videoio.so.4.11.0",
        "libopencv_videoio.so.411",
    ]

    mac_blacklist = ["libOrbbecSDK.1.9.4.dylib", "libOrbbecSDK.1.9.dylib"]

    for filename in windows_blacklist:
        p = path.join(directory, "windows", "bin", filename)
        print(f"removing {p}")
        fs.remove_file(p)
    for filename in nix_blacklist:
        p = path.join(directory, "nix", "bin", filename)
        print(f"removing {p}")
        fs.remove_file(p)
    for filename in mac_blacklist:
        p = path.join(directory, "mac", "bin", filename)
        print(f"removing {p}")
        fs.remove_file(p)
    return True


def _merge_local_hawk_ffi(resources_dir: str) -> bool:
    if not cargo.build("./hawk_ffi/Cargo.toml"):
        return False
    artifact_name = None
    if utils.is_windows():
        artifact_name = "hawk_ffi.dll"
        resources_dir = path.join(resources_dir, "windows")
    if utils.is_linux():
        artifact_name = "libhawk_ffi.so"
        resources_dir = path.join(resources_dir, "nix")
    if utils.is_mac():
        artifact_name = "libhawk_ffi.dylib"
        resources_dir = path.join(resources_dir, "mac")
    artifact_path = path.join(
        ".", "hawk_ffi", "target", "release", artifact_name)
    resources_dir = path.join(resources_dir, "lib")
    destination = path.join(resources_dir, artifact_name)

    if not fs.create_directory(resources_dir):
        return False and fs.merge_into(resources_dir, artifact_path)
    shutil.copyfile(artifact_path, destination)
    return True


def _merge_local_rust_jni(resources_dir: str) -> bool:
    if not cargo.build("./rust_jni/Cargo.toml"):
        return False
    artifact_name = None
    if utils.is_windows():
        artifact_name = "rust_jni.dll"
        resources_dir = path.join(resources_dir, "windows")
    if utils.is_linux():
        artifact_name = "librust_jni.so"
        resources_dir = path.join(resources_dir, "nix")
    if utils.is_mac():
        artifact_name = "librust_jni.dylib"
        resources_dir = path.join(resources_dir, "mac")
    artifact_path = path.join(".", "rust_jni",
                              "target", "release", artifact_name)
    resources_dir = path.join(resources_dir, "lib")
    destination = path.join(resources_dir, artifact_name)

    if not fs.create_directory(resources_dir):
        return False and fs.merge_into(resources_dir, artifact_path)
    shutil.copyfile(artifact_path, destination)
    return True


def build_imagej(local_rust_jni: bool) -> bool:
    imagej_resources = path.join(".", "imagej", "src", "main", "resources")
    if not _gather_tools_into(imagej_resources):
        return False
    if local_rust_jni:
        return _merge_local_rust_jni(imagej_resources)
    return _merge_jhawk(imagej_resources)


def build_pyhawk(local_hawk_ffi: bool) -> bool:
    py_hawk_resources = path.join(".", "py-hawk", "src", "py_hawk")
    if local_hawk_ffi:
        return _merge_local_hawk_ffi(py_hawk_resources)
    return _merge_hawk_ffi(py_hawk_resources)


def build_nap_hawk() -> bool:
    nap_hawk_resources = path.join(".", "nap-hawk", "src", "nap_hawk")
    return _merge_hawk_ffi(nap_hawk_resources)
