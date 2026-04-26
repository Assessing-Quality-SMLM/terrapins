package com.coxphysics.terrapins.models.hawk;
import java.util.ArrayList;
import ij.io.FileSaver;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class PStream extends VirtualStack{


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
    private final Config config_;
    private ArrayList<HawkImageParameters> image_parameters_;
    

    private PStream(ImageStack stack, Config config)
    {
        super(stack.getWidth(), stack.getHeight());
        stack_ = stack;
        config_ = config;

        final int N = stack_.getSize();
        final int levels = config_.n_levels;
        final boolean interleave = config_.output_style_interleaved;

        // Precompute all the image parameters so we can just look it up 
        // with indexing later
        ArrayList<ArrayList<HawkImageParameters>> params = new ArrayList<ArrayList<HawkImageParameters>>();

        if(interleave){
            for(int i=0; i < N; i++)
                params.add(new ArrayList<HawkImageParameters>());
        }
        else{
            params.add(new ArrayList<HawkImageParameters>());
        }

        for(int l=0; l < levels; l++)
		{
			final int kernel_w = 2 << l;
			final int kernel_half_w = 1 << l;

            System.out.printf("Level = %d", l);
			for(int s=0; s < N-kernel_w+ 1; s++)
			{
                System.out.printf("  image = %d", s);
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
        
        image_parameters_ = new ArrayList<HawkImageParameters>();
        // Collate them into the single array
		for(int i=0; i < params.size(); i++)
			for(int j=0; j < params.get(i).size(); j++)
				image_parameters_.add(params.get(i).get(j));
    }

    public static PStream from(ImageStack stack, Config config)
    {
        return new PStream(stack, config);
    }

    public static PStream from(Settings settings)
    {
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
        return from(image.getStack(), config);
    }

    public String get_metadata()
    {   
        //FIXME(ER) put some useful metadata in here?
        System.out.println("TODO: metadata isn't done");
        return "";
    }

    public boolean write_to_disk(String filename)
    {
		// This is pretty redundant now we use standard ImageJ saving
		// probably remove it later.
		// Actually I think this is misplaced because this is an ImageStack
		// which doesn't have the metadata, so anything saved from here will
		// be missing that.
        if (filename == null)
            return false;
		ImagePlus imp = new ImagePlus("", this);
		FileSaver fs = new FileSaver(imp);
		fs.saveAsTiff(filename);
        return true;
    }

    /** Returns the number of slices in this stack. */
    @Override
    public int getSize()
    {
        return image_parameters_.size();
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

    @Override
    public Object getPixels(int n)
    {
        return getProcessor(n).getPixels();
    }

    @Override
    public ImageProcessor getProcessor(int n)
    {
        FloatProcessor fp = new FloatProcessor(getWidth(), getHeight());
        
        //Imagej indexes from 1. All our arrays are Java standard, so from 0
        n--;
        
        final int l = image_parameters_.get(n).layer;
        final int s = image_parameters_.get(n).index + 1;  // 1- indexing
        Split split = image_parameters_.get(n).split;


        final int kernel_w = 2 << l;
        final int kernel_half_w = 1 << l;
        final int h = getHeight();
        final int w = getWidth();


		for(int i=0; i < kernel_half_w; i++){
			// getVoxel does not work for VirtualStacks
			// so work one slice at a time.
			ImageProcessor i1 = stack_.getProcessor(s+i);
			ImageProcessor i2 = stack_.getProcessor(s+i+kernel_half_w);

			for(int r=0; r < h; r++)
				for(int c=0; c<w; c++)
					fp.setf(c,r, fp.getf(c,r) +i1.getf(c,r)-i2.getf(c,r));
		}

		for(int r=0; r < h; r++)
			for(int c=0; c<w; c++){
				float val = fp.getf(r,c);
				if(split == Split.Abs)
					val = Math.abs(val);
				else if(split == Split.Positive)
					val = Math.max(0, val);
				else /* if(split == Split.Negative)*/
					val = Math.max(0, -val);
				fp.setf(r,c,val);
			}

        return fp;
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
