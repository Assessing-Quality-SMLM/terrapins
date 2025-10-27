package com.coxphysics.terrapins.models.hawk;
import com.coxphysics.terrapins.views.hawk.HawkUI;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

public class PStreamFilter implements ExtendedPlugInFilter {
    Config config_ = null;

    ImagePlus image_ = null;

    int n_frames_ = 0;

    int output_size_ = 0;

    int flags_ = STACK_REQUIRED | NO_CHANGES | DOES_16 | DOES_32 | DOES_8G;

    public PStreamFilter()
    {

    }

    private PStreamFilter(ImagePlus image, Config config)
    {
        image_ = image;
        config_ = config;
    }

    public static PStreamFilter from(ImagePlus image, Config config)
    {
        PStreamFilter filter = new PStreamFilter(image, config);
        filter.setup("", image);
        filter.output_size_ = config.get_output_size(filter.n_frames_);
        return filter;
    }

    @Override
    public int showDialog(ImagePlus imagePlus, String s, PlugInFilterRunner plugInFilterRunner)
    {
        HAWKDialog gd = new HAWKDialog(n_frames_);
        gd.showDialog();
        if(!gd.wasOKed())
            return flags_;
        config_ = HawkUI.create_config(gd);
        output_size_ = config_.get_output_size(n_frames_);
        return flags_;
    }

    @Override
    public void setNPasses(int i)
    {

    }

    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        image_ = imagePlus;
        if (image_ == null)
            return flags_;
        n_frames_ = image_.getStack().getSize();
        return flags_;
    }

    @Override
    public void run(ImageProcessor imageProcessor)
    {
        ImagePlus view = get_image_plus();
        if (view == null)
            return;
        view.show();
    }

    public ImagePlus get_image_plus()
    {
        if (image_ == null || config_ == null)
            return null;
        int n_pixels = image_.getWidth() * image_.getHeight();
        PStream new_stack = PStream.from(image_.getStack(), config_, output_size_, n_pixels);
        ImagePlus view = new ImagePlus("JHAWK pstream", new_stack);
        view.setCalibration(get_calibration());
        String metadata = new_stack.get_metadata();
        view.setProp("hawk_metadata", metadata);
        return view;
    }

    private Calibration get_calibration()
    {
        Calibration base = image_.getCalibration().copy();
        base.frameInterval = 0;
        return base;
    }

    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = PStreamFilter.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        // open the Clown sample
//        ImagePlus image = IJ.openImage("C:\\Users\\k1651658\\Documents\\support\\images\\HDVee.tif");
        ImagePlus image = IJ.openImage("/home/nik/Documents/support/images/HDVee.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}
