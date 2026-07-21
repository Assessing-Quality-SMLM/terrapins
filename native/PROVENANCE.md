# Provenance of vendored code under native/

## native/rust

The Rust crates under `native/rust/` were consolidated into this repository from
[sibling repositories](https://github/com/Assessing-Quality-SMLM). Provenance is:

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

Differences are:

- `Cargo.toml`: intra-project dependencies changed to local dependences 
- `frc_this/src/main.rs`: crate rename `rust_frc` -> `smlm_frc` (3 sites).
  The repo's `frc_this/src/corrections.rs` was not copied; it is dead code
  (`// mod corrections;` is commented out upstream).
- `split/src/main.rs`: crate rename `locs` -> `smlm_locs`.

## native/cpp/

Code comes from [hawkman](https://github.com/Assessing-Quality-SMLM/hawkman)
commit  `e00f563`

`CMakeLists.txt` was trimmed to just the hawkman and squirrel targets, and the
extra tools and `cpp/lib_tests/` were not copied.

Note: hawkman gained three cpp commits after the consolidation snapshot
(`5028202` cold-to-hot colour scheme, `2aa9824` saturation, `d74e808` blur
normalisation, 2026-07-07/08, touching `hue_calculator` and `hawkman.cpp`)
which are NOT in this repository.
