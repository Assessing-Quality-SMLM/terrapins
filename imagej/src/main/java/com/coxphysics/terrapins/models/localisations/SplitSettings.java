package com.coxphysics.terrapins.models.localisations;

import com.coxphysics.terrapins.models.reports.EqualSettings;
import com.coxphysics.terrapins.models.reports.Helper;
import com.coxphysics.terrapins.models.reports.Writable;
import com.coxphysics.terrapins.models.reports.WritableString;

import java.io.IOException;
import java.io.Writer;

import static com.coxphysics.terrapins.models.utils.StringUtils.EMPTY_STRING;

public class SplitSettings implements Writable, EqualSettings
{
    private final static String HALF = "half";
    private final static String ZIP = "zip";
    private final static String RANDOM = "rand";

    private String output_path_1_ = EMPTY_STRING;
    private String output_path_2_ = EMPTY_STRING;

    private ParseMethod parse_method_;

    private String method_ = HALF;

    private SplitSettings(ParseMethod parse_method)
    {
        parse_method_ = parse_method;
    }

    public static SplitSettings with_parse_method(ParseMethod parse_method)
    {
        return new SplitSettings(parse_method);
    }

    public static SplitSettings default_()
    {
        return with_parse_method(ParseMethod.default_());
    }

    public String output_path_1()
    {
        return output_path_1_;
    }

    public void set_output_1(String value)
    {
        output_path_1_ = value;
    }

    public String output_path_2()
    {
        return output_path_2_;
    }

    public void set_output_2(String value)
    {
        output_path_2_ = value;
    }

    public String method()
    {
        return method_;
    }

    public void set_half_split()
    {
        set_method(HALF);
    }

    public void set_zip_split()
    {
        set_method(ZIP);
    }

    public void set_random_split()
    {
        set_method(RANDOM);
    }

    public void set_method(String value)
    {
        method_ = value;
    }

    public String parse_method()
    {
        return parse_method_.parse_method();
    }

    public void set_parse_method(ParseMethod parse_method)
    {
        parse_method_ = parse_method;
    }

    public ParseMethod parse_method_settings()
    {
        return parse_method_;
    }

    @Override
    public void write_to(Writer writer)
    {
        try
        {
            Helper.write_setting(writer, "Method", WritableString.from(method()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean has_same_settings(Object other)
    {
        if (!Helper.is_valid_for_comparison(this, other))
            return false;
        return method_.equals(((SplitSettings) other).method_);
    }
}
