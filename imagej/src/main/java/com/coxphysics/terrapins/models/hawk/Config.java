package com.coxphysics.terrapins.models.hawk;

import ij.gui.GenericDialog;

public final class Config
{
    public final String filename;
    public final int n_levels;
    public final boolean negative_handling_separate;
    public final boolean output_style_interleaved;

    private static int default_n_levels()
    {
        return 3;
    }

    public Config(String filename_, int n_levels_, boolean negative_handling_separate_, boolean output_style_interleaved_)
    {
        filename = filename_;
        n_levels = n_levels_;
        negative_handling_separate = negative_handling_separate_;
        output_style_interleaved = output_style_interleaved_;
    }

    public static Config from(Settings settings)
    {
        return new Config(settings.filename(), settings.n_levels(), settings.is_separate(), settings.is_temporal());
    }


    public static Config default_()
    {
        int n_levels = default_n_levels();
        return new Config(null, n_levels, false, false);
    }

    public int get_output_size(int n_frames)
    {
        // Cadged from Ed's old hawkplugin 
        //Compute the number of frames in the output
        int target_size=0;
        for(int l=0; l < n_levels; l++)
        {
            final int kernel_w = 2 << l;
            target_size += (n_frames-kernel_w+1);
        }
        if(negative_handling_separate)
            target_size*=2;

        return target_size;
    }
    
    // TODO(ER): remove these
    public boolean has_output_filename_set()
    {
        return filename != null;
    }

    public String get_validation_errors(int n_frames)
    {
        return "";
    }
}
