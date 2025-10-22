package com.coxphysics.terrapins.models.hawkman;

import com.coxphysics.terrapins.models.UtilsKt;
import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.coxphysics.terrapins.models.filters.image.*;


public class util
{
    public static int psf_to_int(double value)
    {
        return double_to_int_ceil(value);
    }

    public static int double_to_int_ceil(double value)
    {
        return (int)Math.ceil(value);
    }

    public static int psf_half_kernel_size_from_settings(Settings settings)
    {
        return psf_half_kernel_size(settings.psf_size());
    }

    public static int psf_half_kernel_size(int psf_size)
    {
        double quarter_half_psf = psf_size / 4.0;
        double quarter_as_int = Math.ceil(quarter_half_psf);
        return (int) (2 * quarter_as_int - 1);
    }

    public static int blur_kernel_size(int scale)
    {
        double size = ((double)scale) / 2.0;
        int int_size = (int) Math.ceil(size);
        return (2 * int_size) - 1;
    }

    public static double sigma_from_scale_number(int scale)
    {
        return (double)scale / 2.355;
    }

     public static void analyse_scales(Settings settings, Input input)
     {
         if (settings.flatten_images())
         {
             input = flatten_images(input, 256);
         }

         // imgTestHalfFP
         FloatProcessor half_psf_test = create_half_psf_image(settings, input.test_image());

         // imgRefHalfFP
         FloatProcessor half_psf_ref = create_half_psf_image(settings, input.ref_image());

         if (half_psf_test == null || half_psf_ref == null)
         {
             IJ.log("HAWKMAN : Failed to create half psf images");
             return;
         }

         int max_scale = settings.max_scale();

         Output output = Output.create(input.width(), input.height(), max_scale);
         output.add_to_stack(UtilsKt.immutable_clone(input.test_image()));
         output.add_to_stack(UtilsKt.immutable_clone(input.ref_image()));

         HAWKMANData data = new HAWKMANData(input, half_psf_test, half_psf_ref);
         for (int scale = 1; scale <= max_scale; scale++)
         {
             analyse_scale(settings, data, output, scale);
             IJ.showProgress(scale, max_scale);
             if (IJ.escapePressed())
             {
                IJ.log("HAWKMAN : Finishing early at scale level : " + scale);
                break;
            }
         }
         IJ.showStatus("HAWKMAN generating results");
         output.finalise();
         output.dispaly_results(settings.diagnonse());
         IJ.log("HAWKMAN : Analysis Completed");
         IJ.showStatus("HAWKMAN done");
     }

     @Nullable
     private static FloatProcessor create_half_psf_image(Settings settings, FloatProcessor image)
     {
         int kernel_size = psf_half_kernel_size_from_settings(settings);
         FloatProcessor new_image = half_psf_with(image, kernel_size);
         if (new_image != null)
         {
             new_image.resetMinAndMax();
         }
         return new_image;
     }

    @Nullable
    public static FloatProcessor half_psf_with(FloatProcessor image, int kernel_size)
    {
        FloatProcessor new_image = UtilsKt.immutable_clone(image);
        normalise(new_image);
        return box_filter(new_image, kernel_size);
    }

    private static void analyse_scale(Settings settings, HAWKMANData data, Output output, int scale)
     {
         FloatProcessor test_image = data.test_image();
         FloatProcessor ref_image = data.ref_image();
         int SRpixelsX = test_image.getWidth();
         int SRpixelsY = test_image.getHeight();

         double sigma = sigma_from_scale_number(scale);
         int rconv = blur_kernel_size(scale);

         //imgTestFP
         FloatProcessor test_threshold_image = immutable_blur_image_to_scale(data.test_image(), sigma, rconv);

         //imgRefFP
         FloatProcessor ref_threshold_image = immutable_blur_image_to_scale(data.ref_image(), sigma, rconv);

         output.add_to_stack(test_threshold_image.duplicate());
         output.add_to_stack(ref_threshold_image.duplicate());

         BinariseParameters binarisation_settings = settings.binarisation_parameters();
         BinariseParameters skeletonisation_settings = settings.skeletonise_parameters();

         BinariseData test_binary_data = new BinariseData(test_threshold_image, data.half_psf_test(), test_image, binarisation_settings, skeletonisation_settings);
         BinariseData ref_binary_data = new BinariseData(ref_threshold_image, data.half_psf_ref(), ref_image, binarisation_settings, skeletonisation_settings);

         BinarisationResult result = binarise(test_binary_data, ref_binary_data, SRpixelsX,  SRpixelsY);

         new DilateErode().dilate_erode(result, scale, settings.dilate_erode_method());

         ByteProcessor test_binary_image = result.test_binary_image();
         ByteProcessor ref_binary_image = result.ref_binary_image();
         float correlation = CoxGroupLocalCorr.CorrelatePatch(test_binary_image, ref_binary_image);
         output.set_correlation_for(test_binary_image, ref_binary_image, scale, correlation);

         //TestPicBP
         ByteProcessor test_skeleton = skeletonise(test_binary_image);

         //RefPicBP
         ByteProcessor ref_skeleton = skeletonise(ref_binary_image);

         //imgTempA
         ByteProcessor test_blur_skeleton = immutable_gaussian_blur(test_skeleton, sigma);
         test_blur_skeleton.resetMinAndMax();

         //imgTempB
         ByteProcessor ref_blur_skeleton = immutable_gaussian_blur(ref_skeleton, sigma);
         ref_blur_skeleton.resetMinAndMax();

         float skeleton_correlation = CoxGroupLocalCorr.CorrelatePatch(test_blur_skeleton, ref_blur_skeleton);

         ByteProcessor test_skeleton_out = settings.blur_skeletons() ? test_blur_skeleton.convertToByteProcessor() : test_skeleton;
         ByteProcessor ref_skeleton_out = settings.blur_skeletons() ? ref_blur_skeleton.convertToByteProcessor() : ref_skeleton;

         ByteProcessor xor_image = xor_images(test_skeleton, ref_skeleton);
         if (settings.blur_skeletons())
         {
             FloatProcessor blur_base = xor_image.convertToFloatProcessor();
             //ImgTempC
             xor_image = immutable_gaussian_blur(blur_base, sigma).convertToByteProcessor();
             xor_image.resetMinAndMax();
         }
         output.set_skeleton_correlation_for(test_skeleton_out, ref_skeleton_out, xor_image, scale, skeleton_correlation);

         output.add_to_stack_with_label(test_blur_skeleton.duplicate(), "Blurred test image skeleton scale = " + scale);
         output.add_to_stack_with_label(ref_blur_skeleton.duplicate(), "Blurred Ref image skeleton scale = " + scale);

         FloatProcessor skeleton_map =  new CoxGroupLocalCorr().CorrelationMap(test_skeleton, ref_skeleton, scale);
         output.add_to_stack_with_label(skeleton_map, "Correlation map of un-blurred skeletons scale = " + scale);

         FloatProcessor blurred_skeleton_map =  new CoxGroupLocalCorr().CorrelationMap(test_blur_skeleton, ref_blur_skeleton, scale);
         output.add_to_stack_with_label(blurred_skeleton_map, "Correlation map of blurred skeletons scale = " + scale);

         long sr_info_a = non_zero_pixels(test_skeleton);
         long sr_info_b = non_zero_pixels(ref_skeleton);
         output.set_super_resolution_info(scale, sr_info_a, sr_info_b);

         // this is duplication of 6 lines up
         // FloatProcessor blurred_skeleton_map = new CoxGroupLocalCorr().CorrelationMap(test_blur_skeleton, ref_blur_skeleton, scale);
         output.set_skeleton_confidence_map(blurred_skeleton_map); // sets ConfMapSkel
         output.add_to_stack_with_label(blurred_skeleton_map.duplicate(), "Correlation map of blurred skeletons scale = " + scale);

         int range = 2 * scale;
         FloatProcessor binary_map = new CoxGroupLocalCorr().CorrelationMap(result.test_binary_image(), result.ref_binary_image(), range);
         output.set_confidence_map(binary_map); // set ConfMapRes
         output.add_to_stack_with_label(binary_map.duplicate(), "Correlation map of binarised images scale = " + scale);

         set_thresholds_from_maps(test_skeleton,
                                  binary_map,
                                  test_threshold_image,
                                  ref_skeleton,
                                  blurred_skeleton_map,
                                  ref_threshold_image);
         double image_score = image_score(correlation, skeleton_correlation);
         output.set_image_correlation(scale, test_skeleton, ref_skeleton, image_score);
     }

     private static void set_thresholds_from_maps(ImageProcessor test_skeleton,
                                                  FloatProcessor test_confidence_map,
                                                  FloatProcessor test_threshold,
                                                  ImageProcessor ref_skeleton,
                                                  FloatProcessor skeleton_confidence_map,
                                                  FloatProcessor reference_threshold)
     {
         int width = test_skeleton.getWidth();
         int height = test_skeleton.getHeight();
         for(int col=0; col < width; col++)
         {
             for(int row=0; row < height; row++)
             {
                 double thresh_1 = 0.5D / 0.85D;
                 double tempDblA = thresh_1 * skeleton_confidence_map.getf(col, row);
                 if (tempDblA > 0.5D)
                     tempDblA = 0.5D;

                 double tempDblB = thresh_1 * test_confidence_map.getf(col, row);
                 if (tempDblB >  0.5D)
                     tempDblB = 0.5D;

                 double normInt = 127.0D * test_threshold.getf(col, row) * (1.0D - tempDblA - tempDblB);
                 normInt = normInt + (127.0D * reference_threshold.getf(col, row) *  (1.0D - tempDblA - tempDblB));
                 if (normInt > 255.0D)
                     normInt = 255.0D;
                 if (normInt < 0.0D)
                     normInt = 0.0D;
                 test_skeleton.setf(col, row, (int)normInt);

                 normInt = 127.0D * test_threshold.getf(col, row) * (0.0D + tempDblA + tempDblB);
                 normInt = normInt + (127.0D * reference_threshold.getf(col, row) *  (0.0D + tempDblA + tempDblB));
                 if (normInt > 255.0D)
                     normInt = 255.0D;
                 if (normInt < 0.0D)
                     normInt = 0.0D;
                 ref_skeleton.setf(col, row, (int)normInt);
             }
         }
     }

     public static double image_score(float res_corr, float skel_corr)
     {
         return get_value(res_corr) + get_value(skel_corr);
     }

    private static double get_value(float corr)
    {
        double scale_factor = 0.5D / 0.85D; // 10 / 17 = 0.5882352941
        double value = scale_factor * corr;
        return Math.min(value, 0.5D);
    }


     private static ByteProcessor skeletonise(ByteProcessor image)
     {
         ByteProcessor skeleton = UtilsKt.immutable_clone(image);
         skeleton.invert();
         skeleton.skeletonize();
         skeleton.invert();
         return skeleton;
     }

    private static FloatProcessor immutable_blur_image_to_scale(FloatProcessor image, double sigma, int rconv)
    {
         FloatProcessor blurred_image = immutable_gaussian_blur(image, sigma);
         blurred_image.resetMinAndMax();

         // this is also double normalising
         double max = blurred_image.getMax();
         double normaliser = 1.0 / max;
         blurred_image.multiply(normaliser);
         blurred_image.resetMinAndMax();

         FloatProcessor mean_blur = immutable_mean_threshold(blurred_image, rconv);
         mean_blur.resetMinAndMax();
         return mean_blur;
    }

    public static double calculate_binary_threshold(double threshold,
                                                    double smoothing,
                                                    double offset,
                                                    double threshold_image_value,
                                                    double half_psf_value)
    {
        return (threshold * threshold_image_value) + (smoothing * half_psf_value) + offset;
    }

    private static BinarisationResult binarise(BinariseData test_image, BinariseData ref_image, int width, int height)
    {
        BinarisationResult result = new BinarisationResult(width, height);
        for (int row = 0; row < (width - 1); row++)
        {
            for (int col = 0; col < (height - 1); col++)
            {
                BinariseData.Values test_values = test_image.get_binary_values(col, row);
                result.set_test_binary_image(col, row, test_values.get_binary_value());
                result.set_test_skeleton_image(col, row, test_values.get_skeleton_value());

                BinariseData.Values ref_values = ref_image.get_binary_values(col, row);
                result.set_ref_binary_image(col, row, ref_values.get_binary_value());
                result.set_ref_skeleton_image(col, row, ref_values.get_skeleton_value());
            }
        }
        return result;
    }

    public static ByteProcessor xor_images(ByteProcessor image_1, ByteProcessor image_2)
    {
        int width = image_1.getWidth();
        int height = image_1.getHeight();
        ByteProcessor result = new ByteProcessor(width, height);

        for (int col = 0; col < width; col++)
        {
            for (int row = 0; row < height; row++)
            {
                int value_1 = image_1.getPixel(col, row);
                int value_2 = image_2.getPixel(col, row);
                int value = value_1 ^ value_2;
                result.set(col, row, value);
            }
        }
        return result;
    }

    private static Input flatten_images(Input input, int bins)
    {
        FloatProcessor new_test = flatten_image(input.test_image(), bins);
        FloatProcessor new_ref = flatten_image(input.ref_image(), bins);
        return new Input(new_test, new_ref);
    }

    // Function to flattern imput images to 2nd & 98th percentile
    // This isn't true
    public static FloatProcessor flatten_image(ImageProcessor image, int bins)
    {
        FloatProcessor result = image.convertToFloatProcessor();

        result.resetMinAndMax();
        result.findMinAndMax();

        double max_pixel_value = result.getMax();
        int[] hist_result = get_histogram(result, bins);

        long[] cum_sum = cumulative_summation(bins, hist_result);

        double max_bin_value = max_bin_value(bins, cum_sum, max_pixel_value);
        result.max(max_bin_value);

//        IJ.log("MaxBinValue = " + MaxBinValue);

        return result;
    }

    private static double max_bin_value(int bins, long[] cum_sum, double max_pixel_value)
    {
        int max_bin_num = 0;
        for (int idx = 0; idx < bins; idx++)
        {
            if (cum_sum[idx] < (0.98 * cum_sum[bins -1]))
            {
                max_bin_num = idx;
            }
//            if (CumSum[icount] < (0.02*CumSum[bins-1])) {
//                MinBinNum = icount;
//            }
        }

//        Cap image values at below max percentile
//        IJ.log("MaxBinNum = " + max_bin_num + "  MaxPixelVal = " + max_pixel_value);
        return max_pixel_value * (double)max_bin_num / ((double) bins);
    }

    @NotNull
    private static long[] cumulative_summation(int bins, int[] hist_result)
    {
        // Get Comulative sum of histogram and 98th percantile
        long[] cum_sum = new long [bins + 1];
        long total = 0;
        for (int idx = 0; idx < bins; idx++)
        {
            total += hist_result[idx];
            cum_sum[idx] = total;
        }
        return cum_sum;
    }

    @NotNull
    private static int[] get_histogram(ImageProcessor image, int bins)
    {
        double max_pixel_value = image.getMax();
        double min_pixel_value = image.getMin();
        //Histogram pixel values
        int[] hist_result = new int[bins +1];
        for (int col = 0; col < image.getWidth(); col++)
        {
            for (int row = 0; row < image.getHeight(); row++)
            {
                double pixel_value = image.getf(col, row);
                if (pixel_value <= 0.0D)
                {
                    continue;
                }
                double max_bin_value = (pixel_value - min_pixel_value) / (max_pixel_value - min_pixel_value);
                int bin = (int)Math.floor((double) bins * max_bin_value);
                hist_result[bin]++;
            }
        }
        return hist_result;
    }
}
