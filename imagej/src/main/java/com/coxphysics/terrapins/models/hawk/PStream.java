package com.coxphysics.terrapins.models.hawk;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PStream extends ImageStack{


    private enum Split{
        Abs,
        Positive,
        Negative
    };

    private class HawkImageParameters{
        public final int layer;
        public final int index;
        public final Split split;
        public HawkImageParameters(int l_, int i_, Split s_)
        {
            layer=l_;
            index=i_;
            split=s_;
        }
    };

        
    private final ImageStack stack_;
    private final int output_size_;
    private final Config config_;
    private ArrayList<HawkImageParameters> image_parameters_;
    

    private PStream(ImageStack stack, Config config, int output_size)
    {
        super(stack.getWidth(), stack.getHeight());
        System.out.printf("PStream constructor");
        stack_ = stack;
        output_size_ = output_size;
        config_ = config;

        final int N = stack_.getSize();
        final int levels = config_.n_levels;
        final boolean interleave = config_.output_style_interleaved;

        // Precompute all the image parameters so we can just look it up 
        // with indexing later
        ArrayList<ArrayList<HawkImageParameters>> params = new ArrayList<ArrayList<HawkImageParameters>>();

        if(interleave)
            for(int i=0; i < N; i++)
                params.add(new ArrayList<HawkImageParameters>());
        else
            params.add(new ArrayList<HawkImageParameters>());

        for(int l=0; l < levels; l++)
		{
			final int kernel_w = 2 << l;
			final int kernel_half_w = 1 << l;
			final float normalization_factor = (float)Math.sqrt(kernel_w);

			for(int s=0; s < N-kernel_w+ 1; s++)
			{
				int pos_to_add = 0;
				if(interleave)
					pos_to_add = s + kernel_half_w - 1; // This is the "central" frame
				else
					pos_to_add = 0; //Otherwise add things in the order they're computed

                if(config_.negative_handling_separate){
                    params.get(pos_to_add).add(new HawkImageParameters(l, s, Split.Positive));
                    params.get(pos_to_add).add(new HawkImageParameters(l, s, Split.Negative));
                }
                else
                    params.get(pos_to_add).add(new HawkImageParameters(l, s, Split.Abs));
            }
        }
        
        // Collate them into the single array
		for(int i=0; i < params.size(); i++)
			for(int j=0; j < params.get(i).size(); j++)
				image_parameters_.add(params.get(i).get(j));

    }

    public static PStream from(ImageStack stack, Config config, int output_size)
    {
        System.out.println("PStream from 1");
        return new PStream(stack, config, output_size);
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
        System.out.println("Made a pee stream");
        return from(image.getStack(), config, output_size);
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
