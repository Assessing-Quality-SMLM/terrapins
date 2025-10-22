package com.coxphysics.terrapins.models;

import com.coxphysics.terrapins.models.hawk.Config;
import com.coxphysics.terrapins.models.hawk.MetaDataExtract;
import com.coxphysics.terrapins.models.hawk.PStreamFilter;
import ij.ImagePlus;
import ij.ImageStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PStreamFilterTests {

    @Test
    public void default_ctor() //needed for plugin loader
    {
        PStreamFilter filter = new PStreamFilter();
        assertEquals(true, true);
    }

    @Test
    public void calibration_data_preserved()
    {
        int[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        ImageStack stack = new ImageStack(1, 1, data.length);
        int count = 1;
        for (int value : data)
        {
            float [] pixel_data = {value};
            stack.setPixels(pixel_data, count);
            count++;
        }
        ImagePlus image = new ImagePlus("image", stack);
        image.getCalibration().setUnit("fake_unit");
        Config config = Config.from(1, "abs", "");
        PStreamFilter filter = PStreamFilter.from(image, config);
        ImagePlus filtered_image = filter.get_image_plus();
        assertEquals(filtered_image.getCalibration().getUnit(), "fake_unit");
    }

    @Test
    public void frame_interval_zeroed_on_calibration_data()
    {
        int[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        ImageStack stack = new ImageStack(1, 1, data.length);
        int count = 1;
        for (int value : data)
        {
            float [] pixel_data = {value};
            stack.setPixels(pixel_data, count);
            count++;
        }
        ImagePlus image = new ImagePlus("image", stack);
        image.getCalibration().setUnit("fake_unit");
        image.getCalibration().frameInterval = 100;
        assertEquals(image.getCalibration().frameInterval, 100);
        Config config = Config.from(1, "abs", "");
        PStreamFilter filter = PStreamFilter.from(image, config);
        ImagePlus filtered_image = filter.get_image_plus();
        assertEquals(filtered_image.getCalibration().getUnit(), "fake_unit");
        assertEquals(filtered_image.getCalibration().frameInterval, 0);
    }

    @Test
    public void image_has_metadata()
    {
        int[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        ImageStack stack = new ImageStack(1, 1, data.length);
        int count = 1;
        for (int value : data)
        {
            float [] pixel_data = {value};
            stack.setPixels(pixel_data, count);
            count++;
        }
        ImagePlus image = new ImagePlus("image", stack);
        Config config = Config.from(1, "abs", "");
        PStreamFilter filter = PStreamFilter.from(image, config);
        ImagePlus filtered_image = filter.get_image_plus();
        String metadata = MetaDataExtract.get_metadata(filtered_image);
        String expected = "hawk_core:\n" +
                "Version: 0.1.0\n" +
                "config: {\"Version1\":{\"threading\":\"Parallel\",\"memory\":\"Contiguous\",\"run_style\":\"InMemory\",\"algorithm\":{\"n_levels\":1,\"output_style\":\"Sequential\",\"negative_handling\":\"Absolute\"},\"validation\":{\"LimitOutputsToUnder32Bits\":true}}}";
        assertEquals(metadata, expected);
    }
}
