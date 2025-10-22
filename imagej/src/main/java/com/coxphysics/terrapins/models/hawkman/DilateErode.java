package com.coxphysics.terrapins.models.hawkman;

public class DilateErode
{

    public enum Method
    {
        None,
        A,
        B
    }

    public DilateErode()
    {
    }

    public void dilate_erode(BinarisationResult result, int scale, Method method)
    {
        switch (method)
        {
            case None:
                break;
            case A:
                new A().execute(result, scale);
                break;
            case B:
                new B().execute(result, scale);
                break;
        }
    }

    private static abstract class Technique
    {
        public abstract void execute(BinarisationResult result, int scale);

        protected int scale_to_limit(int scale)
        {
            return (int)Math.ceil(scale/1);
        }
    }
    private static class A extends Technique
    {
        @Override
        public void execute(BinarisationResult result, int scale)
        {
            for(int idx = 0; idx <= scale_to_limit(scale); idx++)
            {
                erode_(result);
            }
        }

        private static void erode_(BinarisationResult result)
        {
            result.test_skeleton_image().dilate();
            result.test_skeleton_image().erode();
            result.test_binary_image().dilate();
            result.test_binary_image().erode();

            result.ref_skeleton_image().dilate();
            result.ref_skeleton_image().erode();
            result.ref_binary_image().dilate();
            result.ref_binary_image().erode();
        }
    }

    private static class B extends Technique
    {
        @Override
        public void execute(BinarisationResult result, int scale)
        {
            int limit = scale / 2;
            for(int idx = 0; idx <= limit; idx++)
            {
                result.ref_skeleton_image().dilate();
                result.ref_binary_image().dilate();

                result.test_skeleton_image().dilate();
                result.test_binary_image().dilate();
            }
            for(int idx = 0; idx <= limit; idx++)
            {
                result.ref_binary_image().erode();
                result.test_binary_image().erode();

                result.ref_skeleton_image().erode();
                result.test_skeleton_image().erode();
            }
        }
    }
}
