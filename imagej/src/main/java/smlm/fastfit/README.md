# Fast moment-based SMLM fitter

A non-iterative single-molecule localisation fitter for ImageJ, translated
from the MATLAB `STORMfitter.m` prototype. Detection and fitting sit behind
interfaces, so either can be swapped for a ThunderSTORM implementation
without touching the other.

For each candidate spot it estimates the local background from a perimeter
ring, subtracts it, forms two moments per axis from the patch column and row
sums, and evaluates pre-computed polynomials. No iteration, no solver, no
matrix algebra in the inner loop — about forty floating point operations per
molecule, against roughly ten thousand for a Levenberg–Marquardt Gaussian
fit.

```
frame
  -> baseline subtract
  -> DoG band-pass         \
  -> local maxima          / SpotDetector
  -> ring background       \
  -> moments               |  SubPixelFitter
  -> polynomials           /
  -> localisation table
```

---

## Installing

Copy **`Fast_Fitter.jar`** into your ImageJ or Fiji `plugins/` folder and
restart (or `Help > Refresh Menus`). Two entries appear under
`Plugins > Fast SMLM`:

- **Fast moment fitting** — asks only for pixel size and PSF width, then
  writes the CSV automatically beside the image.
- **Fast moment fitting (advanced)...** — exposes threshold, baseline,
  width bias correction and output options.

The jar is built for **Java 8** bytecode, so it runs on any ImageJ 1.x or
Fiji installation. It needs nothing but `ij.jar`.

### Do not rename the jar without an underscore

ImageJ only scans plugin jars whose **filename contains an underscore**.
From `ij/Menus.java`:

```java
// jarFiles: JAR files in plugins folder with "_" in their name
private static Vector jarFiles;
...
} else if (hasUnderscore
        && (name.endsWith(".jar") || name.endsWith(".zip"))) {
```

A jar without one is never opened, so its `plugins.config` is never read and
nothing appears in any menu — **with no error message anywhere**. Renaming
`Fast_Fitter.jar` to `fast-fitter.jar` silently disables it. (This is why
`Thunder_STORM.jar` is named the way it is.) The Maven build sets
`<finalName>Fast_Fitter</finalName>` to keep this correct.

If the menu entries do not appear, check in order:

1. The jar filename still contains an underscore.
2. It is directly in `plugins/`, or at most one subdirectory deep — ImageJ
   does not recurse further.
3. `Help > Refresh Menus`, or restart ImageJ.
4. `Plugins > Utilities > ImageJ Properties...` shows the plugins directory
   ImageJ is actually using, which may not be the one you copied into.

## Getting your image in

The plugin works on the **currently active image window**. There is no file
chooser — this is the normal ImageJ convention, and it means the plugin
composes with anything else that produces an image.

1. Open your camera data:
   - `File > Open...` for a multi-page TIFF, or
   - `File > Import > Image Sequence...` for a folder of single-frame files
2. Click that image window so it is in front.
3. Run `Plugins > Fast SMLM > Fast moment fitting`.
4. Enter the camera pixel size and the PSF FWHM (both in nanometres) and the
   rendering magnification (default 5).

A super-resolution reconstruction is displayed when the run finishes, and
the CSV is written **beside the source image**, named
`<image>-fastfit.csv`, together with `<image>-fastfit-protocol.txt`
recording the settings used. If the image has no directory on disk, the
working directory is used instead. Existing files are never overwritten: a
numeric suffix is added (`-2`, `-3`, ...).

If the image carries a spatial calibration in nm or µm, the pixel size field
is pre-filled from it. **Confirm the value** — a wrong calibration silently
rescales every coordinate in the output.

### From a macro, including headless batch runs

```
open("/data/experiment1.tif");
run("Fast moment fitting", "pixel_size_nm=100 psf_fwhm_nm=250");
```

This works under `ImageJ --headless` as well. ImageJ 1.x normally cannot run
a plugin dialog headlessly, because `GenericDialog` builds its AWT window in
the constructor, before it looks at the macro options — so any plugin that
constructs one unconditionally throws `HeadlessException` even when a macro
supplied every parameter. This plugin checks for macro options first and
skips the dialog entirely, so batch runs work without Fiji's `ij1-patcher`.

The advanced entry point accepts `threshold_sd`, `min_local_snr`,
`scale_threshold_to_frame_size`, `channel`, `z_slice`,
`baseline`, `auto_baseline`, `width_bias_px` and `save_csv` in the same way.

### From a script

```java
ImagePlus imp = IJ.openImage("/data/experiment1.tif");

FastFitConfig cfg = new FastFitConfig(100.0, 250.0);  // pixel nm, FWHM nm
FastLocalizer localizer = new FastLocalizer(cfg);
List<Localization> results = localizer.run(imp);

LocalizationCsvWriter.write(
        results, new File("/data/out.csv"), localizer.describeRun());
```

Everything except pixel size and PSF FWHM is derived from those two numbers
by `FastFitConfig.derive()`.

---

## Supported image types

| Input | Supported | Notes |
|---|---|---|
| 16-bit stack | Yes | Normal case: EMCCD / sCMOS |
| 12-bit camera data | Yes | Arrives as 16-bit; nothing to set |
| 32-bit float stack | Yes | Fine, e.g. gain-corrected |
| 8-bit stack | Warns | Quantisation hurts width + background |
| RGB / colour | **No** | Rejected; see below |
| Hyperstack (C/Z/T) | One channel | Safety net; see below |
| Virtual stack | Warns | Disk-bound; load to RAM if you can |
| Single frame | Yes | Baseline must be set manually |

### Bit depth: nothing to declare

**12-bit needs no special handling, and 16-bit is not required.** ImageJ has
no 12-bit type, so a 12-bit camera writes into a 16-bit container: the data
arrives as an ordinary 16-bit image holding values in 0–4095, or left-shifted
to fill 0–65535 depending on the acquisition software. Both work, and you do
not tell the plugin which.

The reason is that every moment the fitter computes is a **ratio** of sums of
the same pixels, so multiplying all values by a constant cancels. Verified
over a 400x range of gain: identical localisation counts, and recovered
positions differing by less than 0.0001 nm. Bit depth, camera gain and any
linear rescaling are all irrelevant to position and width.

The one consequence: only the `intensity` and `offset` columns carry the
camera's units. Positions and widths do not depend on them at all, so an
unknown or wrong gain cannot corrupt your coordinates.

Measured on the same synthetic scene at four depths:

| Stored as | Peak value | Localisations | Median sigma |
|---|---|---|---|
| 12-bit range in 16-bit | 804 | 646 | 1.061 px |
| Full-range 16-bit | 12864 | 646 | 1.060 px |
| 32-bit float | 558 | 646 | 1.060 px |
| 8-bit | 51 | 648 | 1.063 px |

### One thing that *does* alter pixel values

If an **intensity calibration function** is set (`Analyze > Calibrate`),
ImageJ applies it during conversion — it is not merely a display setting, so
the fitter sees transformed values. A *linear* calibration is harmless and
cancels out of the moments exactly (measured shift: 0.00 nm). A *non-linear*
one (polynomial, power, log, rodbard) changes the relative weights within a
patch and so changes both the localisation count and the width distribution.
The plugin warns if it finds one; remove it before fitting.

Slices must be **successive camera frames of one field of view**. Anything
else — a z-stack treated as time, or two channels interleaved — produces
localisations that are individually valid but collectively meaningless.

**RGB is rejected rather than converted.** ImageJ's RGB-to-greyscale
conversion is a weighted average, so a pixel reading 200 in one channel
arrives at the fitter as 66.7. That silently destroys the photometry the
moments depend on. If your data really is stored as RGB, convert it
deliberately with `Image > Type > 16-bit` first, or re-export the raw camera
data.

**Hyperstacks are handled as a safety net.** Input for this application is
always single-channel, so this should not arise — but if a hyperstack is
passed in, iterating it blindly would silently mix channels together, because
`getStackSize()` counts every channel and z-slice as well as every time point
(a C=3, Z=4, T=200 hyperstack reports 2400 slices where only 200 are frames).
Rather than produce a plausible-looking but meaningless table, the plugin
enumerates one channel and one z-slice across time, and says so in the log.

---

## Measured performance

Synthetic data with known ground truth, 5000 photons, background 60/pixel,
PSF FWHM 250 nm:

| Pixel size | sigma (px) | Patch | Bias | Position RMS |
|---|---|---|---|---|
| 160 nm | 0.66 | 3x3 | +0.0004 px | **2.2 nm** |
| 130 nm | 0.82 | 3x3 | +0.0001 px | **2.6 nm** |
| 100 nm | 1.06 | 5x5 | -0.0007 px | **3.2 nm** |
| 80 nm | 1.33 | 5x5 | +0.0005 px | **3.1 nm** |

Position error is well below the intrinsic localisation precision of real
SMLM data, so the fast path costs essentially nothing positionally.

Real data (`MicrotubesB.tif`, 500 frames of 128x128, 80 nm pixels): ~43,000
localisations in 1.9 s single-threaded. **Detection accounts for 82% of that**
— the fitting itself runs at roughly 150,000 localisations per second on one
core. Optimisation effort belongs in the detector, not the fitter.

---

## The reconstruction display

The fitted localisations are displayed as an **average shifted histogram**
(ASH) at 5x the camera pixel size by default — so 100 nm camera pixels give a
20 nm rendered pixel, and 80 nm pixels give 16 nm.

A plain histogram at high magnification is dominated by binning artefacts:
whether two nearby localisations land in the same rendered pixel depends on
where the bin edges happen to fall. An ASH removes that dependence by
averaging several histograms whose grids are offset by fractions of a bin.
Averaging *n* shifted histograms is equivalent to accumulating each point
with a separable triangular weight, which is how it is implemented: bin
`(u+i, v+j)` receives `(n-|i|)(n-|j|)`. This matches ThunderSTORM's
`ASHRendering`, including its default of 2 lateral shifts, so images from the
two are directly comparable.

Weights are normalised so each localisation contributes exactly 1.0 and the
image total equals the number of localisations rendered — verified exact on
both synthetic and real data. Brightness is therefore **localisations per
rendered pixel**, a density, not a photon count.

Rendering is essentially free: 43,000 localisations at 5x take about 9 ms.

| Magnification | Output (128x128 input) | Rendered pixel (80 nm camera) |
|---|---|---|
| 2 | 256x256 | 40.0 nm |
| **5** (default) | **640x640** | **16.0 nm** |
| 10 | 1280x1280 | 8.0 nm |
| 20 | 2560x2560 | 4.0 nm |

**Magnification is a display choice and does not improve resolution.** At 5x
the rendered pixel is already below the ~30-50 nm an SMLM reconstruction
typically resolves, so the grid is not the limiting factor; going higher just
makes a larger, sparser image.

Two further notes. The display is contrast-stretched with 0.15% saturation,
because localisation density is very skewed — most pixels are empty and a
few are far brighter, so full-range scaling renders almost black. And unlike
a Gaussian rendering, an ASH does not blur each point by its own precision,
which suits this fitter: it produces no uncertainty estimate to blur by.

When running headless the reconstruction is saved as a calibrated 32-bit
TIFF beside the CSV (`<image>-fastfit-ash5x.tif`) instead of being shown.

## The width estimate — read before filtering on it

`getSigmaPx()` is **not a fitted width**. The width moment
`(left + right) / total` saturates: for a flat patch it tends to a fixed
limit (2/3 for 3x3, 2/5 for 5x5), so once the PSF approaches the patch size
the measurement loses sensitivity. Position uses a *difference* and is
first-order sensitive; width uses a *sum* and is not. Same pixels, very
different conditioning.

Rank correlation against Gaussian fits:

| Condition | Spearman rho |
|---|---|
| Ideal Gaussian PSF, simulated | 0.96 – 0.98 |
| With realistic non-Gaussian tails | 0.70 – 0.91 |
| With a close neighbour (dense data) | 0.37 – 0.64 |
| Real microtubule data, sigma 1.44 px, dense | 0.51 – 0.58 |

It orders spots correctly but compresses the dynamic range. It improves
markedly at smaller PSF sigma: at 100 nm pixels (sigma about 1.05 px) expect
rho around 0.8 to 0.9 on sparse data.

**Use `getWidthClass()`** — a NARROW / NORMAL / WIDE / UNKNOWN
classification — rather than the number. Do not use it for astigmatic z
estimation, and do not present it to users as a fitted sigma.

Real PSF tails add a systematic offset of order **+0.2 px**. Measure it once
per optical configuration by comparing against a Gaussian-fit run, then set
`FastFitConfig.widthBiasCorrectionPx`.

### No uncertainty column

`getUncertaintyNm()` always returns NaN and the CSV column is always empty. A
moment method produces no residual, so there is no goodness of fit; and the
Thompson formula would need a reliable sigma, which this is not. Query
capabilities rather than testing for NaN:

```java
if (fitter.getCapabilities().hasUncertainty()) { ... }
```

A filtering stage shared with a Gaussian fitter should grey out the cuts it
cannot support, rather than silently filtering on empty values.

---

## Substituting ThunderSTORM detection

The core has no dependency on ThunderSTORM. The optional adapter in
`src/adapter/java` wraps ThunderSTORM's filters and detectors:

```java
SpotDetector detector = ThunderStormDetector.wavelet("std(Wave.F1)", 8);
FastLocalizer localizer = new FastLocalizer(cfg, detector, null);
```

Detection is then identical to ThunderSTORM's, so a fast-versus-accurate
comparison isolates the fitter. Build with:

```
mvn -Pthunderstorm -Dthunderstorm.jar=/path/to/Thunder_STORM.jar package
```

**Warning:** ThunderSTORM's `Thresholder` holds process-wide static state, so
the adapter must not run concurrently with ThunderSTORM's own "Run analysis",
nor with a second differently-configured instance. The built-in `DoGDetector`
has no such constraint. Call `dispose()` when finished.

At run time ImageJ's `PluginClassLoader` puts every jar in `plugins/` on one
classpath, so ThunderSTORM only needs to be installed alongside — no shading
required. Do not bundle ThunderSTORM's classes into this jar: load order
across jars is undefined, and two copies of `CameraSetupPlugIn` would mean
two independent copies of its static camera state.

---

## Tuning notes

**`calibrationRangeTolerance`** (default 0.25). The in-range check compares
against a *noiseless* moment envelope, but real moments carry photon noise,
so good spots near the edge scatter outside it. With zero tolerance about 27%
of candidates are rejected on real data and the rate barely responds to the
detection threshold — which looks like a detection problem but is not. At
0.25 it falls to about 4%.

**`thresholdFactor`** (default 3.0). Multiples of a robust standard deviation
of the filtered image, recomputed per frame. Robust rather than ordinary sd,
because bright spots would inflate the latter as activation density rises.
This makes sensitivity mildly data-dependent across a stack, the same
behaviour as ThunderSTORM's `std(Wave.F1)`; use
`DoGDetector.setAbsoluteThreshold` for a fixed threshold.

**Background ring radius** is derived as at least 4 sigma. Closer in, the
ring still carries PSF tails at a level comparable to the read noise, biasing
the background high and dragging the width estimate down.

**Baseline** is measured from the temporal minimum projection by default. On
stacks shorter than about 50 frames this reads low; set `baselineOffset`
explicitly instead.

---

## Differences from the MATLAB prototype

Fixed deliberately rather than ported faithfully:

- **Border candidates are discarded, not clamped.** The MATLAB clamps indices
  to the frame interior, which fits a patch where nothing was detected and
  piles spurious localisations along the edges.
- **`max(x - bg, 0)` instead of `abs(x - bg)`.** Over-subtraction should
  saturate at zero, not reflect background back up into apparent signal. With
  the prototype's `background = 100` on data whose true background is about
  2, `abs()` turns background pixels into 98.
- **Local ring background instead of a global constant.** Background varies
  across the field and across the stack; in `MicrotubesB.tif` the per-frame
  median drifts from 287 to 436 ADU.
- **Calibration range tracks the expected PSF width.** A fixed wide range
  (0.5–2.5 px) gives a 0.13 px polynomial residual; a range tied to the
  expected sigma gives about 0.005 px.
- **Constrained polynomial bases fitted directly.** The prototype fits all 21
  `poly55` terms then evaluates 9 of them. The discarded coefficients do come
  out at zero — the symmetry argument holds — but fitting the constrained
  model is equivalent and unambiguous.
- **Output is a localisation table, not a histogram image.**

---

## Scope

Single-emitter, 2D only. No multi-emitter fitting, no astigmatic 3D, no
post-processing (duplicate removal, merging, drift correction). The
calibration assumes a circular, separable PSF; the single y-slice is valid
only because the common y-factor cancels in both moments.

## Licence

The core carries no ThunderSTORM code and is independently licensable. The
optional adapter links against ThunderSTORM, which is GPLv3 — building and
distributing with `-Pthunderstorm` brings that obligation with it. Worth a
proper legal opinion if any of this is to be commercialised.
