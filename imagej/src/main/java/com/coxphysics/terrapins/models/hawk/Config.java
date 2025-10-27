package com.coxphysics.terrapins.models.hawk;

import ij.gui.GenericDialog;

public class Config
{
    private final int n_levels_;
    private final short negative_handling_;
    private final short output_style_;

    private static short default_output_style()
    {
        return NativeHAWK.output_style_sequential();
    }

    private static short default_negative_handling()
    {
        return NativeHAWK.negative_handling_absolute();
    }

    private static int default_n_levels()
    {
        return 3;
    }

    private Config(int n_levels, short negative_handling, short output_style)
    {
        n_levels_ = n_levels;
        negative_handling_ = negative_handling;
        output_style_ = output_style;
    }

    public static Config with(int n_levels, short negative_handling, short output_style)
    {
        return new Config(n_levels, negative_handling, output_style);
    }

    public static Config from(int n_levels, String negative_handling, String output_style)
    {
        short nh = default_negative_handling();
        short os = default_output_style();

        if(negative_handling.equals("ABS"))
            nh = NativeHAWK.negative_handling_absolute();
        else if(negative_handling.equals("Separate"))
            nh = NativeHAWK.negative_handling_separate();

        if(output_style.equals("Group temporally"))
            os = NativeHAWK.output_style_interleaved();
        return new Config(n_levels, nh, os);
    }


    public static Config default_()
    {
        short nh = default_negative_handling();
        short os = default_output_style();
        int n_levels = default_n_levels();
        return new Config(n_levels, nh, os);
    }

    public String get_validation_errors(int n_frames)
    {
        long config_ptr = get_config_ptr();
        try
        {
            return NativeHAWK.config_validate(config_ptr, n_frames);
        }
        finally
        {
            NativeHAWK.config_free(config_ptr);
        }
    }

    public int get_output_size(int n_frames)
    {
        long config_ptr = get_config_ptr();
        try
        {
            final long n_output_frames_true = NativeHAWK.output_size(config_ptr, n_frames);
            int n_output_frames = 0;
            try
            {
                n_output_frames = Math.toIntExact(n_output_frames_true);
                return n_output_frames;
            }
            catch (ArithmeticException _e)
            {
                GenericDialog gui = new GenericDialog("Error");
                gui.addMessage("Cannot convert output frames of long " + n_output_frames_true + " to int\nthis might be solvable using the command line version\nplease message the developement team for help");
                gui.showDialog();
                return 0;
            }
        }
        finally
        {
            NativeHAWK.config_free(config_ptr);
        }
    }

    private long get_config_ptr()
    {
        return NativeHAWK.config_new(n_levels(), negative_handling(), output_style());
    }

    public long n_levels()
    {
        return n_levels_;
    }

    public short negative_handling()
    {
        return negative_handling_;
    }

    public short output_style()
    {
        return output_style_;
    }
}
