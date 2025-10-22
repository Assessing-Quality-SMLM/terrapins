package com.coxphysics.terrapins.models.reports;

import com.coxphysics.terrapins.models.hawk.NativeHAWK;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelperTests
{
    @Test
    public void write_if_set_bool()
    {
        StringWriter writer = new StringWriter();
        Helper.write_if_set_default(writer, "something", true, false);
        String expected = "something: true";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void dont_write_if_not_set_bool()
    {
        StringWriter writer = new StringWriter();
        Helper.write_if_set_default(writer, "something", true, true);
        String expected = "";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void write_if_set_string()
    {
        StringWriter writer = new StringWriter();
        Helper.write_if_set_default(writer, "something", "a", "b");
        String expected = "something: a";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void dont_write_if_not_set_string()
    {
        StringWriter writer = new StringWriter();
        Helper.write_if_set_default(writer, "something", "a", "a");
        String expected = "";
        assertEquals(writer.toString(), expected);
    }
}