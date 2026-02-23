import _build

from dev_ops import gh, cargo

import sys

# before running this CI architecture should have asembeled the binary objects


def run() -> bool:
    version_file = _build.rust_ffi_toml()
    new_version = cargo.bump_minor(version_file)
    if new_version is None:
        return False
    return gh.commit_version_bump("hawk_ffi", new_version, version_file, "./artifacts.zip")


if run():
    print("ok")
    sys.exit(0)
else:
    print("fail")
    sys.exit(1)
