package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.ffi;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static com.coxphysics.terrapins.models.ffi.is_windows;

public class FRC
{

    private final static String FFTW = "libfftw3-3";

    private final Path fftw_dll_path_;

    private FRC(Path fftw_dll_path)
    {
        fftw_dll_path_ = fftw_dll_path;
    }

    public static FRC custom(Path fftw_dll_path)
    {
        return new FRC(fftw_dll_path);
    }

    public static FRC default_()
    {
        Path fftw_dll_path = extract_dependencies();
        return custom(fftw_dll_path);
    }

    @Nullable
    public static Path extract_dependencies()
    {
        Path fftw_dll_path = null;
        if (is_windows())
        {
            String fftw_name = ffi.windows_dll_name(FFTW);
            fftw_dll_path = ffi.extract_resource_to_temp(FRC.class, fftw_name, true, false);
        }
        return fftw_dll_path;
    }

    public boolean is_valid()
    {
        return if_dll_required_we_have_it();
    }

    private boolean if_dll_required_we_have_it()
    {
        if (is_windows())
        {
            return fftw_dll_path_ != null;
        }
        return true;
    }
}
