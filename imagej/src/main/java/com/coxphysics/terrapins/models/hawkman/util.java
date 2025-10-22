package com.coxphysics.terrapins.models.hawkman;

import com.coxphysics.terrapins.models.UtilsKt;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.coxphysics.terrapins.models.filters.image.*;


public class util
{

    public static int psf_half_kernel_size(int psf_size)
    {
        double quarter_half_psf = psf_size / 4.0;
        double quarter_as_int = Math.ceil(quarter_half_psf);
        return (int) (2 * quarter_as_int - 1);
    }

    public static double sigma_from_scale_number(int scale)
    {
        return (double)scale / 2.355;
    }

    @Nullable
    public static FloatProcessor half_psf_with(FloatProcessor image, int kernel_size)
    {
        FloatProcessor new_image = UtilsKt.immutable_clone(image);
        normalise(new_image);
        return box_filter(new_image, kernel_size);
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


    public static double calculate_binary_threshold(double threshold,
                                                    double smoothing,
                                                    double offset,
                                                    double threshold_image_value,
                                                    double half_psf_value)
    {
        return (threshold * threshold_image_value) + (smoothing * half_psf_value) + offset;
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
