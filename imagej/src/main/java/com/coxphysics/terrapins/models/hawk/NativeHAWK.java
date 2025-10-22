package com.coxphysics.terrapins.models.hawk;

import com.coxphysics.terrapins.models.ffi;

import java.nio.file.Path;

public class NativeHAWK 
{
    private static Path get_dll_path()
    {
        String name = ffi.os_dll_name("rust_ffi");
        return ffi.extract_resource_to_temp(NativeHAWK.class, name, false, true);
    }

    public static Boolean init()
    {
        Path p = get_dll_path();
        if (p == null)
            return false;
        // System.out.println("Loading " + p);
        System.load(p.toString());
        return true;
    }
    
    static
    {
        init();
    }

    public static native short output_style_sequential();
    public static native short output_style_interleaved();

    public static native short negative_handling_absolute();
    public static native short negative_handling_separate();

    public static native long config_new(long n_levels, short negative_handling, short output_style);
    public static native String config_validate(long config_ptr, long n_frames);
    public static native void config_free(long ptr);

    public static native String get_metadata(long config_ptr);
    
    public static native long output_size(long config_ptr, long n_frames);

    public static native float[] hawk_stream_get_image_float(Object stack_wrapper, long config_ptr, int stream_index, int n_pixels);
}
