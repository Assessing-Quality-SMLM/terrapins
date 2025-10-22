import _build
import tests

from dev_ops import maven

import sys


def run() -> bool:
    use_bleeding_edge_dll = False
    # test framework with non local settings will assemble the package
    # and run the tests
    # after this we can bump the version number, re-run tests?
    # tag the repo and sling to upload site (manually?).
    return _build.build_imagej(use_bleeding_edge_dll) and \
        maven.install(tests.imagej_pom())


if run():
    print("ok")
    sys.exit(0)
else:
    print("fail")
    sys.exit(1)
