package com.coxphysics.terrapins.models.utils;

import java.util.Objects;

public final class StringUtils
{
    public final static String EMPTY_STRING = "";

    public static boolean path_set(String path)
    {
        return is_set(path);
    }

    public static boolean is_set(String path)
    {
        return path != null && !path.equals(EMPTY_STRING);
    }

    public static Integer parse_unisigned_int(String value)
    {
        try
        {
            return Integer.parseUnsignedInt(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static Character to_char(String value)
    {
        try
        {
            return value.charAt(0);
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
    }

    public static Double parse_double(String value)
    {
        if (Objects.equals(value, "inf"))
            return Double.POSITIVE_INFINITY;
        if (Objects.equals(value, "-inf"))
            return Double.NEGATIVE_INFINITY;
        return Double.parseDouble(value);
    }
}
