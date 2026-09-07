# Provenance of vendored code under imagej/

The same arrangement as `native/PROVENANCE.md`, for Java and Kotlin sources brought in from
elsewhere rather than written here.

## smlm.fastfit

The fast moment fitter, under `src/main/java/smlm/fastfit/` and `src/test/java/smlm/fastfit/`.

| source repo | commit    |
|-------------|-----------|
| [Fastfitting](https://github.com/sc383/Fastfitting) | `34fb7f4` |

**This copy is temporary and expected to be removed.** An improved fitter is in testing which
corrects systematic bias present in this one, trading a slightly slower fit for substantially
better precision on low background data. The moment fitter is here to make the one-click path
testable end to end in the meantime, not because it is the intended fitter.

Two things follow. Do not invest in improving it here - upstream is where the current fitter is
maintained, and the replacement is a different codebase. And do not treat this directory as a
mirror to be kept in step indefinitely; it is a scaffold with a known end date.

It is compiled from source rather than depended on as an artifact. It had briefly been a Maven
dependency resolved from a developer's local repository, which built on that machine and nowhere
else - CI has no such artifact, so the build broke for everyone but its author. Vendoring is what
the Rust crates already do, and it keeps a checkout of this repository sufficient to build.

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

Upstream remains the place to develop the current fitter. When taking a newer version, copy the
two source directories again, update the commit above, and re-run `PrecisionCalibrationTest` - it
measures the estimator against simulated ground truth and is the thing that notices if a change
to the moment calibration, the patch sizing rule or the background ring geometry has moved the
precision estimate.

### When the replacement arrives

The seam it plugs into is `models/oneclick/Fitter`, and the one-click workflow asks a fitter what
it can do rather than assuming, so a swap should not reach beyond that interface.

The part that does not transfer is the precision calibration. `PrecisionEstimator` carries eight
constants fitted against *this* fitter's scatter, and they describe how far it falls short of the
Thompson bound - which is precisely what the new fitter changes. Carrying them over would report
a precision the new fitter does not have, and on low background data, where the improvement is
said to be largest, it would be wrong in the optimistic direction.

`PrecisionCalibrationTest#fullCalibrationGrid` is the harness that measured them and will measure
them again: it fits the two coefficients per patch regime against simulated ground truth. Whether
that model still fits is itself a question - `ratioIsStableAcrossConditions` is the test that
answers it, and it failed twice for the current fitter before the model was right.
