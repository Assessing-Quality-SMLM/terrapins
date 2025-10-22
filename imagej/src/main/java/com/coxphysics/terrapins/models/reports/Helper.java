package com.coxphysics.terrapins.models.reports;

import java.io.IOException;
import java.io.Writer;

public class Helper
{
    public static <T> void write_if_set_default(Writer writer, String name, T value, T default_value)
    {
        write_if_set(writer, name, WritableString.from(value), WritableString.from(default_value));
    }

    public static <T extends Writable & EqualSettings> void write_if_set(Writer writer, String name, T value, T default_value)
    {
        try
        {
            if (value.has_same_settings(default_value))
                return;
            write_setting(writer, name, value);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Writable> void write_setting(Writer writer, String name, T value) throws IOException
    {
        writer.write(String.format("%s: ", name));
        value.write_to(writer);
    }

    public static boolean is_valid_for_comparison(Object a, Object b)
    {
        return a != null && b != null && a.getClass() == b.getClass();
    }
}
