package com.coxphysics.terrapins.models;

import com.coxphysics.terrapins.models.utils.FsUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ffi
{
    public static String MAC_NAME = "Mac OS X";

    private static String get_os_name()
    {
        return System.getProperty("os.name");
    }

    public static boolean os_is_windows(String os_name)
    {
        return os_name.startsWith("Windows");
    }

    public static boolean os_is_mac(String os_name)
    {
        return os_name.startsWith("Mac");
    }

    public static boolean is_windows()
    {
        return os_is_windows(get_os_name());
    }

    public static boolean is_mac()
    {
        return os_is_mac(get_os_name());
    }

    public static String windows_dll_name(String dll)
    {
        return dll + ".dll";
    }

    public static String nix_dll_name(String dll)
    {
        return "lib" + nix_dll_name_with_extension(dll);
    }

    public static String nix_dll_name_with_extension(String dll)
    {
        return dll + ".so";
    }

    public static String windows_exe_name(String exe)
    {
        return exe + ".exe";
    }

    public static String nix_exe_name(String exe)
    {
        return exe;
    }

    public static String mac_dll_name(String dll)
    {
        return "lib" + mac_dll_name_with_extension(dll);
    }

    public static String mac_dll_name_with_extension(String dll)
    {
        return dll + ".dylib";
    }

    public static String mac_exe_name(String exe)
    {
        return nix_exe_name(exe);
    }

    public static String os_dll_name(String dll)
    {
        if (is_windows())
            return windows_dll_name(dll);
        if (is_mac())
            return mac_dll_name(dll);
        return nix_dll_name(dll);
    }

    public static String os_exe_name(String exe)
    {
        if (is_windows())
            return windows_exe_name(exe);
        if (is_mac())
            return mac_exe_name(exe);
        return nix_exe_name(exe);
    }

    private static String get_resource_directory(boolean in_bin)
    {
        return in_bin ? "bin" : "lib";
    }

    public static String windows_resource_path_as_string(String resource_name, boolean in_bin)
    {
        // have to do this as Paths.get on windows works in tests but not when imageJ loads plugin jar
        // I do not know why
        return "windows/" + get_resource_directory(in_bin) +"/" + resource_name;
    }

    public static String nix_resource_path_as_string(String resource_name, boolean in_bin)
    {
        return Paths.get("nix", get_resource_directory(in_bin), resource_name).toString();
    }

    public static String mac_resource_path_as_string(String resource_name, boolean in_bin)
    {
        return Paths.get("mac", get_resource_directory(in_bin), resource_name).toString();
    }

    private static String os_resource_path(String resource_name, boolean in_bin)
    {
        if (is_windows())
            return windows_resource_path_as_string(resource_name, in_bin);
        if (is_mac())
            return mac_resource_path_as_string(resource_name, in_bin);
        return nix_resource_path_as_string(resource_name, in_bin);
    }

    private static Path temp_location_for(String resource_name)
    {
        return  FsUtils.temp_directory().resolve(resource_name);
    }

    private static Path copy_to_temp(String resource_name, InputStream resource_stream, boolean in_bin, boolean is_exe)
    {
        String resource_path = os_resource_path(resource_name, in_bin);
        Path destination = temp_location_for(resource_name);
        try
        {
            long result = Files.copy(resource_stream, destination, StandardCopyOption.REPLACE_EXISTING);
            boolean permissions_set_ok = set_permissions(is_exe, destination);
            return destination;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean set_permissions(boolean is_exe, Path destination) throws IOException
    {
        File f = new File(destination.toString());
        boolean readable_ok = f.setReadable(true, true);
        if (is_exe)
        {
            boolean exe_ok = f.setExecutable(true, true);
            return readable_ok && exe_ok;
        }
        else
        {
            return readable_ok;
        }
    }

    public static String resource_name(String resource_path)
    {
        return "/" + resource_path;
    }

    public static Path extract_resource_to_temp(Class class_, String resource_name, boolean in_bin, boolean is_exe)
    {
        String p = os_resource_path(resource_name, in_bin);
        String name = resource_name(p);
        InputStream in = class_.getResourceAsStream(name);
        if (in == null)
            return null;
        return copy_to_temp(resource_name, in, in_bin, is_exe);
    }
}
