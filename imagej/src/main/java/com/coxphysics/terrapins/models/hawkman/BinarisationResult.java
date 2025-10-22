package com.coxphysics.terrapins.models.hawkman;

import ij.process.ByteProcessor;

class BinarisationResult
    {
        // imgTestRes
        private final ByteProcessor test_binary_image_;

        // imgTestSkel
        private final ByteProcessor test_skeleton_image_;

        // imgRefRes
        private final ByteProcessor ref_binary_image_;

        // imgRefSkel
        private final ByteProcessor ref_skeleton_image_;

        BinarisationResult(int width, int height)
        {
            test_binary_image_ = new ByteProcessor(width, height);
            test_skeleton_image_ = new ByteProcessor(width, height);
            ref_binary_image_ = new ByteProcessor(width, height);
            ref_skeleton_image_ = new ByteProcessor(width, height);
        }

        public ByteProcessor test_binary_image()
        {
            return test_binary_image_;
        }

        public void set_test_binary_image(int col, int row, int value)
        {
            test_binary_image_.set(col, row, value);
        }

        public ByteProcessor test_skeleton_image()
        {
            return test_skeleton_image_;
        }

        public void set_test_skeleton_image(int col, int row, int value)
        {
            test_skeleton_image_.set(col, row, value);
        }

        public ByteProcessor ref_binary_image()
        {
            return ref_binary_image_;
        }

        public void set_ref_binary_image(int col, int row, int value)
        {
            ref_binary_image_.set(col, row, value);
        }

        public ByteProcessor ref_skeleton_image()
        {
            return ref_skeleton_image_;
        }

        public void set_ref_skeleton_image(int col, int row, int value)
        {
            ref_skeleton_image_.set(col, row, value);
        }
    }
