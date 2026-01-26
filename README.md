# TERRAPINS - TEsting Resolution, Reliability And Performance IN SMLM 

This tool is for assessing 2D SMLM data quality and reliability. Many issues can affect the quality of this data, this tool aims to give a you both a measure of quality but, also how to tune your experimental setup and highlight the limiting factors of its performance.


# Processing Flow chart
![Processing flow chart](./guide/images/processing_flow_chart.png)


What do I need to do/steps


## Main types of error tested for
 - [Drift](#drift-report)
 - [Sampling](#sampling-report)
 - [Blinking](#blinking-report)
 - [Magnification](#magnification-report)
 - [Bias](#bias-report)
 - [Linearity](#linearity-report)
 - [Everything Else](#everything-else-report)

# I want to?

 - [Assess my images to help inform my experimental setup](#assess-my-images-to-help-inform-my-experimental-setup)
 - [Assess my images for a limiting precision score](#assess-my-images-for-a-limiting-precision-score)

## Assess my images to help inform my experimental setup
 - [I am using localisation data](#assess-my-images-to-help-inform-my-experimental-setup-using-localisation-data)
 - [I am using images](#assess-my-images-to-help-inform-my-experimental-setup-using-image-data)

## Assess my images for a limiting precision score
 - [I am using localisation data](#assess-my-images-for-a-limiting-precision-score-using-localisation-data)
 - [I am using images](#assess-my-images-for-a-limiting-precision-score-using-image-data)

## Assess my images to help inform my experimental setup using localisation data
## Assess my images to help inform my experimental setup using image data

## Assess my images for a limiting precision score using localisation data
## Assess my images for a limiting precision score using image data

# Drift Report
# Sampling Report
# Blinking Report
# Magnification Report
# Bias Report
# Linearity Report
# Everything Report

# How to get the plugin
Latest ImageJ plugin can be found [here](https://github.com/Assessing-Quality-SMLM/terrapins/releases/latest)

<!-- # What's the point / Scope? 
A number of techniqeus exist for assessing the quality of data produced in a super resolution experiment however, they all measure something slightly different. Alongside this in order to fully intepret their results sometimes information from other tools are required. This means they should be used together to provide a fuller picture of any issues that may be present in your data. 

Whilst several of these tools exist in isolation, this project aims to provide all the tools in one place, accessible across operating systems and analysis platforms to be integrated into users workflows as they see fit. To this end these tools are accessible as ImageJ plugins, Napari plugins and command line tools as well as being open source for anyone to compile as they see fit (we can also create some C API shared objects / dlls / dylibs for integration into other tools if required). 

# Who's it for?
Anyone doing super-res! However, we are focussing providing easy to use tools for people new to the technique.  -->

# FAQ

## My HAWKMAN is being rejected but it looks fine to me /SRRF didn’t throw an error. Is it actually okay?
HAWKMAN failure modes, what to do, but it’s probably just bad


## Fixed pattern noise is being picked up as structure (esp SRRF). Is this really a problem? What do I do?
Most common when SRRF is being used in low density situations. Perhaps try a single or multi emitter fitter.

## My images look fine. Why is the bias report flagging an issue?
The bias report uses the HAWKMAN assessment. This technique can sometimes go awry, particualry on very sparse data during the skeletonisation step. You can manually inspect what the tool has done by heading to the results tab and opening the skeletonisation images and the scores. 

The bias score is determined from global score which weights contributions from each of the maps. It should be increasing linearly as the levels increase if the scores do not do this something has gone wrong. 

If, when you look at the skeletonised images, they are not representative then you can discount this score. 

## My FRC scores are not what I expect?
FRC is based on a Fourier transform; consequently there are 2 main sources of errors, missalignment when cropping a ROI or if your data is highly structured, particularly over long ranges, e.g. a structural motif is repeated across the image. 

In the case of cropping an image take your time and be accurate, FRC can produce unexpected results with single pixel missalignments. Also be sure to crop all the data you are using; widefields, image stacks, recons and recon localisation lists etc.

In the case of highly structured data artefacts can be introduced into the spectrum which can be hard accomodate. If you are only interested in a particular part of the image try cropping it out and running the assessment on that.

Also make sure you have [handled your fiducial markers appropriately](#have-i-excluded-fiducial-markers)

## Is My Data Suitable for Assessment?
This can depend on the exact research question below are some general steps you can take before interpretting the assessment output.

### Is there any data?
Open your images up and look at the histogram. Is what you expect?


### Has my pre-processing been applied correctly?

#### Have I drift corrected effectively?
The drift report can help with this. Some datasets respond better than others to different drift correction techniques as well as their settings. If one technique / settings are not improving the drift report try something else. 

#### Did my fitter do something sensible?
What does the localisation table look like? Localisations should be realistic; psf sigmas should be neither too high nor too low, likewise uncertainty - internally we filter these properties which can remove lots of data if it was not great to begin with. N.B processing can effect these properties as well, for example HAWK processing whilst helping to remove bias will decrese precision.

#### Do I need to merge localisations?
If your reconstructed image shows lots of clustering it may be that localisations need to be merged, or merged more aggressively.

#### Have I excluded fiducial markers?
Fiducial markers can introduce several issues when retained in the assessment images. For example, in FRC based measures they will be in both sets of images regardless of splitting technique masquerading as structure. Equally in the bias test they will distort the adaptive threshold that gets applied to the images. 

We would advise you keep them in for performing drift correction and they can be left in for localising / fitting. However, you will need to either crop out a part of the image without these or mask them out before the assessment. We would reccomend croping them out if possible as masking can introduce other sources of artefact.

<!-- SQUIRREL widefield needs them to be blocked out - common mask. with common mask in widefield will introduce a sharp edge which wont be in super-res with effects of background dominating in this region -->



<!-- # Usage

## ImageJ

### I have Localisation Data

### I have Image Data

## I want to use an Individual Tool

## Napari
Watch this space

## CLI

# It doesn't work / I Have a Problem 
This is actively support so please raise an issue. 


# I Want to Build My Own Binaries
 -->