import _build
import tests

from dev_ops import gh, maven

import argparse
import sys


def bump_imagej_version_number(pom: str) -> bool:
    return maven.bump_minor(pom)


def run(dry_run: bool) -> bool:
    use_bleeding_edge_dll = False
    # test framework with non local settings will assemble the package
    # and run the tests
    # after this we can bump the version number, re-run tests?
    # tag the repo and sling to upload site (manually?).
    pom = tests .imagej_pom()
    build_ok = _build.build_imagej(use_bleeding_edge_dll)
    if not build_ok:
        return False

    new_version_number = bump_imagej_version_number(pom)
    if new_version_number is None:
        return False

    if not maven.install(pom):
        return False

    return gh.commit_version_bump("TERRAPINS", new_version_number, pom, "./artifacts.zip", dry_run)


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
