# Provenance of vendored code under imagej/

The same arrangement as `native/PROVENANCE.md`, for Java and Kotlin sources brought in from
elsewhere rather than written here.

## smlm.fastfit

The fast moment fitter, under `src/main/java/smlm/fastfit/` and `src/test/java/smlm/fastfit/`.

| source repo | commit    |
|-------------|-----------|
| [Fastfitting](https://github.com/sc383/Fastfitting) | `34fb7f4` |

It is compiled from source here rather than depended on as an artifact. It had briefly been a
Maven dependency resolved from a developer's local repository, which built on that machine and
nowhere else - CI has no such artifact, so the build broke for everyone but its author. Vendoring
is what the Rust crates already do, and it keeps a checkout of this repository sufficient to
build.

Differences from upstream:

- `src/adapter/java/smlm/fastfit/adapter/ThunderStormDetector.java` was **not** copied. It lets
  ThunderSTORM's filters and detectors stand in for the built-in detector, and upstream keeps it
  behind a Maven profile because it needs a ThunderSTORM jar to compile. Nothing here uses it
  yet. It belongs with the ThunderSTORM integration when that lands, not before.
- `src/main/resources/plugins.config` was not copied. ImageJ reads one such file per jar, so the
  fitter's two menu entries live in this module's own `src/main/resources/plugins.config`
  instead.
- Nothing else is changed. The package is still `smlm.fastfit`, so the menu entries, the
  `FastFitterAdapter` and any macro written against the fitter keep working unaltered.

`README.md` beside the sources is upstream's, and documents the fitter itself - what the width
estimate is and is not, the photon calibration, and the measured performance.

### Keeping it in step

Upstream remains the place to develop the fitter. When taking a newer version, copy the two
source directories again, update the commit above, and re-run `PrecisionCalibrationTest` - it
measures the estimator against simulated ground truth and is the thing that notices if a change
to the moment calibration, the patch sizing rule or the background ring geometry has moved the
precision estimate.
