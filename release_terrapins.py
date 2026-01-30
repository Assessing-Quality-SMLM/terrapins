import _build

from dev_ops import fs, gh, maven

import argparse
import sys


PROJECT = "TERRAPINS"

def bump_imagej_version_number(pom: str) -> bool:
    return maven.bump_minor(pom)


def run(dry_run: bool) -> bool:
    # test framework with non local settings will assemble the package
    # and run the tests
    # after this we can bump the version number, re-run tests?
    # tag the repo and sling to upload site (manually?).
    use_bleeding_edge_dll = False
    build_ok = _build.build_imagej(use_bleeding_edge_dll)
    if not build_ok:
        return False

    pom = _build.imagej_pom()
    new_version_number = bump_imagej_version_number(pom)
    if new_version_number is None:
        return False

    if not fs.remove_directory(_build.imagej_target()):
        return False

    if not maven.install(pom):
        return False

    new_artifact = _build.dependency_build_of(PROJECT, new_version_number)
    deployment_location = _build.image_j_deployment_path(PROJECT, new_version_number)
    fs.copy_file(new_artifact, deployment_location)

    if not gh.commit_version_bump(PROJECT, new_version_number, pom, deployment_location, dry_run):
        return False

    fs.remove_file(deployment_location)
    return True


parser = argparse.ArgumentParser(prog="release_terrapins", description=f"Release the TERRAPINS package")
parser.add_argument("-l", "--live-run", action="store_true")
namespace = parser.parse_args()
live_run = namespace.live_run
print(f"Live run: {live_run}")
dry_run = not live_run
if run(dry_run):
    print("ok")
    sys.exit(0)
else:
    print("fail")
    sys.exit(1)
