package com.coxphysics.terrapins.models.hawk;

import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PStream extends ImageStack implements AutoCloseable{
    private final ImageStack stack_;
    private final long config_ptr_;
    private final int output_size_;
    private final int n_pixels_;

    private PStream(ImageStack stack, long config_ptr, int output_size, int n_pixels)
    {
        super(stack.getWidth(), stack.getHeight());
        stack_ = stack;
        output_size_ = output_size;
        n_pixels_ = n_pixels;
        config_ptr_ = config_ptr;
    }

    public static PStream from(ImageStack stack, Config config, int output_size, int n_pixels)
    {
        long config_ptr = NativeHAWK.config_new(config.n_levels(), config.negative_handling(), config.output_style());
        return new PStream(stack, config_ptr, output_size, n_pixels);
    }

    public String get_metadata()
    {
        return NativeHAWK.get_metadata(config_ptr_);
    }

    public boolean write_to_disk(String filename)
    {
        StackWrapper wrapper = StackWrapper.from_stack(stack_);
        return NativeHAWK.hawk_to_file(wrapper, config_ptr_, stack_.getHeight(), stack_.getWidth(), filename);
    }

    /** Returns the number of slices in this stack. */
    @Override
    public int getSize()
    {
        return output_size_;
    }

    /** Returns the bit depth (8, 16, 24 or 32), or 0 if the bit depth is not known. */
    @Override
    public int getBitDepth()
    {
        return 32;
    }

    @Override
    public boolean isVirtual()
    {
        return true;
    }


    // These are imageJ 1-based indexes
    // Rust wants zero based
    @Override
    public Object getPixels(int n)
    {
        int rust_index = n - 1;
        StackWrapper wrapper = StackWrapper.from_stack(stack_);
        return NativeHAWK.hawk_stream_get_image_float(wrapper, config_ptr_, rust_index, n_pixels_);
    }

    @Override
    public ImageProcessor getProcessor(int n)
    {
        Object data = getPixels(n);
        return new FloatProcessor(getWidth(), getHeight(), (float[]) data);
    }

    @Override
    public void close() throws Exception
    {
        NativeHAWK.config_free(config_ptr_);
    }

    /** FAKE OUT SOME OTHER METHODS*/

    @Override
    public void addSlice(ImageProcessor ip) {

    }
    @Override
    public void addSlice(String sliceLabel, Object pixels) {
    }

    /** Does nothing.. */
    @Override
    public void addSlice(String sliceLabel, ImageProcessor ip) {
    }

    /** Does noting. */
    @Override
    public void addSlice(String sliceLabel, ImageProcessor ip, int n) {
    }

    @Override
    public void deleteSlice(int n) {

    }

    @Override
    public void deleteLastSlice() {

    }

    public void setPixels(Object pixels, int n) {
    }


    /** Returns the label of the Nth image. */
    @Override
    public String getSliceLabel(int n) {
        return null;
    }

    /** Returns null. */
    @Override
    public Object[] getImageArray() {
        return null;
    }

    /** Does nothing. */
    @Override
    public void setSliceLabel(String label, int n) {
    }


    /** Does nothing. */
    @Override
    public void trim() {
    }


    /** Sets the bit depth (8, 16, 24 or 32). */
    @Override
    public void setBitDepth(int bitDepth) {

    }
}
