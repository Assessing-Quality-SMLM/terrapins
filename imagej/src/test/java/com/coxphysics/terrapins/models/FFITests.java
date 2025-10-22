package com.coxphysics.terrapins.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FFITests
{
    @Test
    public void is_windows()
    {
        Assertions.assertTrue(ffi.os_is_windows("Windows"));
    }

    @Test
    public void windows_dll_name()
    {
        assertEquals(ffi.windows_dll_name("something"), "something.dll");
    }

    @Test
    public void unix_dll_name()
    {
        assertEquals(ffi.nix_dll_name("something"), "libsomething.so");
    }

        @Test
    public void windows_exe_name()
    {
        assertEquals(ffi.windows_exe_name("something"), "something.exe");
    }

    @Test
    public void unix_exe_name()
    {
        assertEquals(ffi.nix_exe_name("something"), "something");
    }

    @Test
    public void windows_dll_resource_path_as_string()
    {
        assertEquals(ffi.windows_resource_path_as_string("something", false), "windows/lib/something");
        assertEquals(ffi.windows_resource_path_as_string("something", true), "windows/bin/something");
    }

    @Test
    public void is_mac()
    {
        assertTrue(ffi.os_is_mac(ffi.MAC_NAME));
    }

    @Test
    public void mac_dll_name()
    {
        assertEquals(ffi.mac_dll_name("thing"), "libthing.dylib");
    }

    @Test
    public void mac_exe_name()
    {
        assertEquals(ffi.mac_exe_name("thing"), "thing");
    }
}
