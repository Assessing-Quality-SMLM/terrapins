import _build

from dev_ops import cargo, maven, hatch

import argparse
import sys


def rust_ffi_toml() -> str:
    return "./rust_ffi/Cargo.toml"


def rust_jni_toml() -> str:
    return "./rust_jni/Cargo.toml"


def imagej_pom() -> str:
    return "./imagej/pom.xml"


def rust_ffi_tests() -> bool:
    return cargo.test(rust_ffi_toml())


def rust_jni_tests() -> bool:
    return cargo.test(rust_jni_toml())


def rust_tests() -> bool:
    return rust_ffi_tests() and rust_jni_tests()


def java_tests(use_local_ffi_bindings: bool) -> bool:
    if not _build.build_imagej(use_local_ffi_bindings):
        return False
    return maven.test(imagej_pom())


def python_tests(use_local_ffi_bindings: bool) -> bool:
    if not _build.build_pyhawk(use_local_ffi_bindings):
        return False
    return hatch.run_lint_and_test("./py-hawk")  #and hatch.run_lint_and_test("./nap-hawk")


def run_local_tests() -> bool:
    return rust_tests() and \
        java_tests(True)
        # and \
        # python_tests(True)


def run_package_tests() -> bool:
    return java_tests(False) and python_tests(False)


def run(local_tests: bool) -> bool:
    if local_tests:
        return run_local_tests()
    return run_package_tests()


# this needs to be in main as
# imported from release scripts to run package tests
if __name__ == "__main__":
    parser = argparse.ArgumentParser(
                    prog="Tests",
                    description="Test the package using pre-built binaries or local bleeding edge versions")
    parser.add_argument('-l', '--local', action="store_false")
    namespace = parser.parse_args()
    local_tests = not namespace.local
    print(f"Testing local copies: {local_tests}")
    if run(local_tests):
        print("ok")
        sys.exit(0)
    else:
        print("fail")
        sys.exit(1)
