package com.coxphysics.terrapins.models.hawkman;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Output
{
    // stackout
    private final ImageStack stack_out_;

    private final float[] res_corr_;

    private final float[] skeleton_corr_;

    // CorrScore
    private final float[] corr_score_;

    private final float[] sr_info_a_;

    private final float[] sr_info_b_;

    // imagine I can just auto generate this
    private final float[] PlotX; //PlotX[scalenum] = scalenum;

    // stackRGBRes
    private final ImageStack rgb_results_;

    // stackRGBSkel
    private final ImageStack skeleton_rgb_results_;

    // stackConf
    private final ImageStack confidence_results_;

    // ConfMapRes
    private FloatProcessor confidence_map_ = null;

    // ConfMapSkel
    private FloatProcessor skeleton_confidence_map_ = null;

    private Output(ImageStack stack_out,
                   float[] res_corr,
                   float[] skeleton_corr, float[] corrScore,
                   float[] sr_info_a,
                   float[] sr_info_b,
                   float[] plotX,
                   ImageStack rgb_results,
                   ImageStack skeleton_rgb_results, ImageStack confidenceResults)
    {
        stack_out_ = stack_out;
        res_corr_ = res_corr;
        skeleton_corr_ = skeleton_corr;
        corr_score_ = corrScore;
        sr_info_a_ = sr_info_a;
        sr_info_b_ = sr_info_b;
        PlotX = plotX;
        rgb_results_ = rgb_results;
        skeleton_rgb_results_ = skeleton_rgb_results;
        confidence_results_ = confidenceResults;
    }

    public static Output create(int width, int height, int max_scale)
    {
        ImageStack stack_out =  ImageStack.create(width, height, 0, 32);
        stack_out.setVoxel(0, 0, 0, 0.999);

        int length = max_scale + 1;
        float[] res_corr = new float[length];
        float[] skeleton_corr = new float[length];
        float[] corr_score = new float[length];

        float[] sr_info_a = new float[length];
        float[] sr_info_b = new float[length];

        float[] PlotX = new float[]{length};

        ImageStack rgb_results = ImageStack.create(width, height, 0, 24);
        ImageStack skeleton_rgb_results = ImageStack.create(width, height, 0, 24);
        ImageStack confidence_results = ImageStack.create(width, height, 0, 24);

        return new Output(stack_out,
                          res_corr,
                          skeleton_corr,
                          corr_score,
                          sr_info_a,
                          sr_info_b,
                          PlotX,
                          rgb_results,
                          skeleton_rgb_results,
                          confidence_results);
    }

    public void add_to_stack(ImageProcessor image)
    {
        stack_out_.addSlice(image);
    }

    public void add_to_stack_with_label(ImageProcessor image, String label)
    {
        stack_out_.addSlice(image);
        stack_out_.setSliceLabel(label, stack_out_.getSize() - 1);
    }

    public void set_correlation_for(ByteProcessor image_1, ByteProcessor image_2, int scale, float correlation)
    {
        res_corr_[scale] = correlation;
        PlotX[scale] = scale; // prob delete depending on usage

        ColorProcessor colours = new ColorProcessor(image_1.getWidth(), image_1.getHeight());
        colours.setChannel(1, image_1);
        colours.setChannel(2, image_2);
        colours.setChannel(3, util.xor_images(image_1, image_2));
        rgb_results_.addSlice(colours);
        rgb_results_.setSliceLabel("Corr coef = " + correlation, scale + 1);
    }

    public void set_skeleton_correlation_for(ByteProcessor image_1, ByteProcessor image_2, ByteProcessor xor_image, int scale, float correlation)
    {
        skeleton_corr_[scale] = correlation;

        ColorProcessor colours = new ColorProcessor(image_1.getWidth(), image_1.getHeight());
        colours.setChannel(1, image_1);
        colours.setChannel(2, image_2);
        colours.setChannel(3, xor_image);
        skeleton_rgb_results_.addSlice(colours);
        skeleton_rgb_results_.setSliceLabel("Corr coef = " + correlation, scale + 1);
    }

    public void set_super_resolution_info(int scale, long a, long b)
    {
        sr_info_a_[scale] = a;
        sr_info_b_[scale] = b;
    }

    public void set_confidence_map(FloatProcessor confidence_map)
    {
        confidence_map_ = confidence_map;
    }

    public void set_skeleton_confidence_map(FloatProcessor skeleton_confidence_map)
    {
        skeleton_confidence_map_ = skeleton_confidence_map;
    }

    public void set_image_correlation(int scale, ByteProcessor test_skeleton, ByteProcessor ref_skeleton, double score)
    {
        corr_score_[scale] = (float)score;

        ColorProcessor colConf = new ColorProcessor(test_skeleton.getWidth(), test_skeleton.getHeight());
        colConf.setChannel(1, test_skeleton);
        colConf.setChannel(2, ref_skeleton);
        colConf.setChannel(3, ref_skeleton);
        confidence_results_.addSlice(colConf);
    }

    public void finalise()
    {
        // Remove initial slice, output stack to image window
        stack_out_.deleteSlice(1);
        rgb_results_.deleteSlice(1);
        skeleton_rgb_results_.deleteSlice(1);
        confidence_results_.deleteSlice(1);
    }

    public void dispaly_results(Boolean show_test_results)
    {
        if (show_test_results)
        {
            ImagePlus imgOut = new ImagePlus("HAWKMAN test results", stack_out_);
            imgOut.show();
        }
        ImagePlus imgRGBRes = new ImagePlus("HAWKMAN Sharpening map", rgb_results_);
        imgRGBRes.show();
        ImagePlus imgRGBSkel = new ImagePlus("HAWKMAN Structure map", skeleton_rgb_results_);
        imgRGBSkel.show();
        ImagePlus imgRGBConf = new ImagePlus("HAWKMAN Confidence map", confidence_results_);
        imgRGBConf.show();
    }
}
