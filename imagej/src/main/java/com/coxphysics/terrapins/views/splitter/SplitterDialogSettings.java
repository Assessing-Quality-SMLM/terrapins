package com.coxphysics.terrapins.views.splitter;

import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.localisations.SplitSettings;
import com.coxphysics.terrapins.models.utils.StringUtils;

import static com.coxphysics.terrapins.models.utils.StringUtils.EMPTY_STRING;

public class SplitterDialogSettings
{
    private final String localisation_file_;

    private final SplitSettings settings_;

    private SplitterDialogSettings(String localisation_path, SplitSettings settings)
    {
        localisation_file_ = localisation_path;
        settings_ = settings;
    }

    public static SplitterDialogSettings from(String localisation_path, SplitSettings settings)
    {
        return new SplitterDialogSettings(localisation_path, settings);
    }

    public static SplitterDialogSettings default_()
    {
        return from(EMPTY_STRING, SplitSettings.default_());
    }

    public boolean has_localisation_path()
    {
        return StringUtils.path_set(localisation_file_);
    }

    public String localisation_file()
    {
        return localisation_file_;
    }

    public SplitSettings split_settings()
    {
        return settings_;
    }

    public String output_1()
    {
        return settings_.output_path_1();
    }

    public String output_2()
    {
        return settings_.output_path_2();
    }

    public ParseMethod parse_method_settings()
    {
        return settings_.parse_method_settings();
    }

}
