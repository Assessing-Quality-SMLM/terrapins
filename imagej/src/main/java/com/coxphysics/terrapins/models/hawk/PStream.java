package com.coxphysics.terrapins.models.hawk;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PStream extends ImageStack{
    private final ImageStack stack_;
    private final int output_size_;
    private final Config config_;

    private PStream(ImageStack stack, Config config, int output_size, int n_pixels)
    {
        super(stack.getWidth(), stack.getHeight());
        System.out.println("PStream constructor");
        stack_ = stack;
        output_size_ = output_size;
        config_ = config;
    }

    public static PStream from(ImageStack stack, Config config, int output_size, int n_pixels)
    {
        System.out.println("PStream from 1");
        return new PStream(stack, config, output_size, n_pixels);
    }

    public static PStream from(Settings settings)
    {
        System.out.println("Trying to make a p stream");
        Config config = Config.from(settings);
        ImagePlus image = settings.image();
        if (image == null){
            System.out.println("no image in settings");
            return null;
        }
        Integer n_frames = settings.n_frames();
        if (n_frames == null){
            System.out.println("n_frames is null");
            return null;
        }
        int output_size = config.get_output_size(n_frames);
        int n_pixels = image.getWidth() * image.getHeight();
        System.out.println("Made a pee stream");
        return from(image.getStack(), config, output_size, n_pixels);
    }

    public String get_metadata()
    {   
        //FIXME(ER) put some useful metadata in here?
        return "";
    }

    public boolean write_to_disk(String filename)
    {
        if (filename == null)
            return false;
        StackWrapper wrapper = StackWrapper.from_stack(stack_);
        System.out.printf("TODO: wtf wtf save to %s\n", filename);
        //FIXME(ER): actually write here
        return false;
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
        //FIXME(ER): HAWK computation goes here
        System.out.printf("TODO: compute layer %d\n", n);
        return new float[getWidth()*getHeight()];
    }

    @Override
    public ImageProcessor getProcessor(int n)
    {
        Object data = getPixels(n);
        return new FloatProcessor(getWidth(), getHeight(), (float[]) data);
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
