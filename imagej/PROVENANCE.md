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


## com.coxphysics.terrapins.vendored.thunderstorm

ThunderSTORM, under `src/main/java/com/coxphysics/terrapins/vendored/thunderstorm/`.

Taken from a local working copy of [zitmen/thunderstorm](https://github.com/zitmen/thunderstorm)
at `e85c565`, **plus uncommitted changes on top of it** - the fixes for modern JDKs and headless
operation that are the reason for forking at all.

That is not a reproducible provenance, and it should be fixed before release. The checkout it came
from still has upstream as its remote, so those fixes have nowhere to be pushed. Forking under
`Assessing-Quality-SMLM`, committing them, and replacing the line above with a real hash is the
outstanding job.

What was uncommitted at the time of copying, so that it can at least be recognised:

- `AnalysisPlugIn`, `BiplaneAnalysisPlugIn`, `ResultsTableWindow` - waiting for the rendering
  queue to drain before returning to a caller, so a macro cannot save a half-rendered image.
- `ResultsDriftCorrection` - applying the correction before attempting to plot it, so drift
  correction works headlessly instead of aborting on `HeadlessException` and silently leaving the
  table uncorrected.
- `GenericTableWindow`, `PostProcessingModule` - `exitWhenQuitting(false)` and lazy panel
  construction, mitigating an `ij.ImageJ` teardown race that could kill the host process.
- `HelpButton` - catching `Throwable` from the embedded HTML viewer, which fails on any JDK past 8.
- `MLEFitterLM`, `LevenbergMarquardtMLE` and their benchmark - a multi-start heuristic for the
  gradient-based MLE optimiser, which otherwise converges to an inferior local optimum on a
  meaningful fraction of low-photon fits.
- Several estimator UI and calibration files, and `pom.xml` (kotlin 1.9.24, surefire 3.2.5,
  commons-math3 bumped 3.2 to 3.6.1 so it matches the version this module already uses).

Bundled rather than asked of the user. Several ThunderSTORM builds are in circulation, some with
the Java-version and headless problems this fork exists to fix, and requiring a separate download
costs a substantial share of users - which is the whole thing the one-click path is trying not to
do.

### The package is renamed, and that is the point

Every class moved from `cz.cuni.lf1.lge.ThunderSTORM` into
`com.coxphysics.terrapins.vendored.thunderstorm`.

ImageJ puts every jar in `plugins/` on one classloader, built from `File.list()` with no sort,
and resolves each class name to whichever jar it reaches first. A user with ThunderSTORM already
installed would otherwise have two copies of the same 480 class names on that classloader, and
which one ran would depend on filesystem ordering - silently, and differently on different
machines. Worse, this fork adds classes the stock build does not have, so the two could mix:
new code linked against old.

Renaming removes the question. Both copies can be installed and neither can shadow the other.

Four things a package rename does not do on its own, each handled here:

- **Service files.** `ModuleLoader` finds filters, detectors, estimators and post-processing
  modules through `ServiceLoader`, which reads `META-INF/services/<interface>`. Relocation
  renames neither those filenames nor their contents, and the failure is a
  `RuntimeException("No modules of type ... loaded.")` at run time. Both are rewritten.
- **Preferences.** About forty keys are string literals like `"thunderstorm.camera"`, untouched
  by any rename, so both copies would read and write the same ImageJ preferences - a one-click run
  would quietly overwrite a user's own camera setup. All are now under `terrapins.thunderstorm.`.
- **The version resource.** `ThunderSTORM.VERSION` loads `thunderstorm.properties` from the
  classpath root, which would collide with a stock install's copy of the same name. Renamed to
  `terrapins-thunderstorm.properties`.
- **Help pages.** `Help.getResourcePath` derives a path from the class name, so the pages had to
  move with the package or every dialog would log a missing help file.

### Menu commands

`plugins.config` registers only what the one-click path drives, under names of this build's own -
"Run analysis (TERRAPINS)" and so on. A separately installed ThunderSTORM keeps the usual names,
so `IJ.run` is never ambiguous. The rest of its menu is deliberately not exposed; anyone wanting
the whole application installs it separately and gets it unshadowed.

### Local fixes on top of the fork

Changes made here rather than upstream, listed so they are not mistaken for imported code and are
not lost if a newer version is taken.

- **`ImportExportPlugIn.runExport`** - exporting from a macro without naming columns died with
  `NullPointerException: No component was registered for this parameter`. The branch that means
  "nothing was named, so export everything" recorded that by calling `setValue` on each column
  parameter, which writes through to the parameter's Swing component - and macro mode never
  builds one, because it skips showing the dialog. It cannot be avoided from the calling side
  either: the column parameters are named after the literal table headers, so they are things
  like `x [nm]` and `uncertainty_xy [nm]`, which are not expressible as macro option keys.
  Exporting everything is the only thing a macro can ask for. The decision is now held in a local
  rather than pushed back through the UI layer.

  This is the same family as the fixes the fork already carries - work that assumes a dialog
  exists, failing when driven headlessly or from a macro - and is worth taking upstream with them.

### Not vendored

- `UpdaterPlugIn` - a bundled copy has no business updating itself, and it was the only user of
  guava, rxjava, rxkotlin and retrofit, about 3 MB of jar.
- The TSF and Proto exporters and their generated protobuf classes, removed from the
  `IImportExport` services list. Nothing here writes those formats and they cost protobuf.
- `swingbox`, an embedded HTML help viewer loaded by reflection that already fails on any JDK
  past 8 and is caught. `HelpButton` still degrades to "help unavailable" exactly as before.

### Still shared with a stock install

`MacroAwareUI` publishes into `cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI` - inside
ThunderSTORM's namespace despite being a separate artifact - so the vendored sources keep their
imports of that one package, and its classes are still under the original name in the jar. The
same applies to `thunderstorm-algorithms`. Both are small leaf libraries pinned to a commit, so
two copies would be the same code; the collision that mattered was the 480 classes of
ThunderSTORM itself, and that one is gone. Relocating these two as well needs shading rather than
a source rename, and is the obvious next step if it ever bites.

Both come from jitpack, which is a new external repository in this build and worth watching in
CI.
