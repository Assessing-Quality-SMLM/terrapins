package com.coxphysics.terrapins.models.hawk;

import com.coxphysics.terrapins.models.ffi;

import java.nio.file.Path;

public class NativeHAWK 
{

    public static short output_style_sequential(){
		return 0;
	}
    public static short output_style_interleaved(){
		return 1;
	}

    public static short negative_handling_absolute(){
		return 0;
	}

    public static short negative_handling_separate(){
		return 1;
	}

    public static String config_validate(long config_ptr, long n_frames){
		return ""; //ER FIXME. What is this meant to do?
	}

    public static String get_metadata(long config_ptr){
		return ""; // ER FIXME. Is this needed?
	}
    
}
