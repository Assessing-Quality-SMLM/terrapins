package com.coxphysics.terrapins.models.hawkman.snippets;

import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.BlurToScale;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Test;

import static com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Helpers.create_test_image;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BlurToScaleTests
{
    @Test
    public void basic_test()
    {
        ImageProcessor image = create_test_image(10);
        FloatProcessor result = BlurToScale.blur(image, 4);
        float[] expected = new float[]{3.6666667f, 4.3333335f, 5.3333335f, 6.3333335f, 7.3333335f, 8.333333f, 9.333333f, 10.333333f, 11.333333f, 12.0f, 10.333333f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f, 17.0f, 18.0f, 18.666666f, 20.333334f, 21.0f, 22.0f, 23.0f, 24.0f, 25.0f, 26.0f, 27.0f, 28.0f, 28.666666f, 30.333334f, 31.0f, 32.0f, 33.0f, 34.0f, 35.0f, 36.0f, 37.0f, 38.0f, 38.666668f, 40.333332f, 41.0f, 42.0f, 43.0f, 44.0f, 45.0f, 46.0f, 47.0f, 48.0f, 48.666668f, 50.333332f, 51.0f, 52.0f, 53.0f, 54.0f, 55.0f, 56.0f, 57.0f, 58.0f, 58.666668f, 60.333332f, 61.0f, 62.0f, 63.0f, 64.0f, 65.0f, 66.0f, 67.0f, 68.0f, 68.666664f, 70.333336f, 71.0f, 72.0f, 73.0f, 74.0f, 75.0f, 76.0f, 77.0f, 78.0f, 78.666664f, 80.333336f, 81.0f, 82.0f, 83.0f, 84.0f, 85.0f, 86.0f, 87.0f, 88.0f, 88.666664f, 87.0f, 87.666664f, 88.666664f, 89.666664f, 90.666664f, 91.666664f, 92.666664f, 93.666664f, 94.666664f, 95.333336f};
        assertArrayEquals((float[])result.getPixels(), expected);
    }
}