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
    release = "hawkman_and_squirrel_0.10.0"
    return _merge(resources_dir, artifacts, repo, release)


def _merge_assessment(resources_dir: str) -> bool:
    artifacts = "assessment_artifacts"
    repo = "https://github.com/Assessing-Quality-SMLM/assessment/"
    release = "assessment_0.11.0"
    return _merge(resources_dir, artifacts, repo, release)


def _merge_jhawk(resources_dir: str) -> bool:
    jhawk_artifacts = "jhawk"
    jhawk_repo = "https://github.com/Assessing-Quality-SMLM/terrapins/"
    jhawk_release = "jhawk_0.4.0"
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
    return _merge_assessment(directory)


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
