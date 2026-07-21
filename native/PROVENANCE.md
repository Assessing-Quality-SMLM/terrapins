# Provenance of vendored code under native/

The Rust crates under `native/rust/` were consolidated into this repository from
sibling repositories in the Assessing-Quality-SMLM GitHub org (commit `dc2befb`,
"Consilidated the repos", 2026-07-06). Those source repositories are now frozen
and archived.

Provenance was verified 2026-07-16 by hashing every vendored file
(`git hash-object`) and comparing against `git ls-tree -r <commit>` across the
full history of each source repository. Every directory below is byte-identical
to the stated commit's subtree, with the exceptions listed afterwards.

| native/rust dir | source repo : subdir              | commit    |
|-----------------|-----------------------------------|-----------|
| `assessment`    | `assessment` : `assessment/`      | `4e4b1b5` |
| `smlm-qa`       | `assessment` : `smlm-qa/`         | `4e4b1b5` |
| `smlm-renderer` | `renderer` : `rust/smlm-renderer/`| `35517af` |
| `f2i`           | `renderer` : `rust/f2i/`          | `35517af` |
| `smlm-frc`      | `frc` : `smlm-frc/`               | `bf03c0a` |
| `frc_this`      | `frc` : `frc_this/`               | `bf03c0a` |
| `smlm-locs`     | `localisations` : `smlm-locs/`    | `574fe04` |
| `split`         | `localisations` : `splitter/`     | `574fe04` |
| `smlm-imp`      | `imp` : `smlm-imp/`               | `fe95d8c` |
| `smlm-sig-proc` | `signal_processing` : `smlm-sig-proc/` | `568a19e` |
| `smlm-tiff`     | `tiff_wrap` : `smlm-tiff/`        | `3a86ebe` |

Each commit was the source repository's `main` HEAD at the time of
consolidation. Repos live at `github.com/Assessing-Quality-SMLM/<repo>`.

Known deviations from the source commits (no logic changes):

- Every `Cargo.toml`: inter-crate dependencies rewired from git/crates.io
  references to local path dependencies (the point of the consolidation).
- `frc_this/src/main.rs`: crate rename `rust_frc` -> `smlm_frc` (3 sites).
  The repo's `frc_this/src/corrections.rs` was not copied; it is dead code
  (`// mod corrections;` is commented out upstream).
- `split/src/main.rs`: crate rename `locs` -> `smlm_locs`.

## native/cpp/

Verified the same way against the `hawkman` repo:

| native/cpp dir | source repo : subdir | commit    |
|----------------|----------------------|-----------|
| `lib`          | `hawkman` : `cpp/lib`   | `e00f563` |
| `tools`        | `hawkman` : `cpp/tools` | `e00f563` |

`lib` is byte-identical. In `tools`, `hawkman.cpp` and `squirrel.cpp` are
byte-identical; `CMakeLists.txt` was rewritten (trimmed to just the hawkman and
squirrel targets), and the extra tools (`corr_test.cpp`, `squirrel_explorer.cpp`,
`test_cards.cpp`) and `cpp/lib_tests/` were not copied.

Note: hawkman gained three cpp commits after the consolidation snapshot
(`5028202` cold-to-hot colour scheme, `2aa9824` saturation, `d74e808` blur
normalisation, 2026-07-07/08, touching `hue_calculator` and `hawkman.cpp`)
which are NOT in this repository. All other source repositories' final HEADs
are exactly what was vendored.
