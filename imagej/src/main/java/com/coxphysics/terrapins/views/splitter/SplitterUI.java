package com.coxphysics.terrapins.views.splitter;

import com.coxphysics.terrapins.views.Checkbox;
import com.coxphysics.terrapins.views.FileField;
import com.coxphysics.terrapins.views.RadioButtons;
import com.coxphysics.terrapins.views.Utils;
import com.coxphysics.terrapins.views.localisations.ParseMethodsUI;
import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.localisations.SplitSettings;
import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.gui.GenericDialog;

public class SplitterUI
{
    private final static String LOCALISATION_FILE_NAME_ = "Localisation File";
    private final static String OUTPUT_A = "first split output filename (Optional)";
    private final static String OUTPUT_B = "second split output filename (Optional)";


    private final static String HALF = "Half";
    private final static String ZIP = "Zip";
    private final static String RANDOM = "Random";

    private final static String METHOD = "split method";

    private final static String RANDOMISATION = "randomise split";

    private final FileField localisation_file_;
    private final FileField output_a_;
    private final FileField output_b_;
    private final RadioButtons method_;
    private final ParseMethodsUI parse_methods_;

    private SplitterUI(FileField localisation_file_field,
                      FileField output_a,
                      FileField output_b,
                      RadioButtons method,
                      ParseMethodsUI parse_methods)
    {

        localisation_file_ = localisation_file_field;
        output_a_ = output_a;
        output_b_ = output_b;
        method_ = method;
        parse_methods_ = parse_methods;
    }

    public static SplitterUI add_controls_to_dialog(GenericDialog dialog, SplitSettings settings)
    {
        SplitterDialogSettings dialog_settings = SplitterDialogSettings.from(StringUtils.EMPTY_STRING, settings);
        SplitterUI ui = add_to_dialog(dialog, dialog_settings);
        ui.set_localisation_field_visible(false);
        return ui;
    }

    public static SplitterUI add_to_dialog(GenericDialog dialog, SplitterDialogSettings settings)
    {
        FileField localisation_file_field = Utils.add_file_field(dialog, LOCALISATION_FILE_NAME_, settings.localisation_file());
        FileField output_a = Utils.add_file_field(dialog, OUTPUT_A, settings.output_1());
        FileField output_b = Utils.add_file_field(dialog, OUTPUT_B, settings.output_2());
        RadioButtons method = Utils.add_radio_buttons(dialog, METHOD, new String[]{HALF, ZIP, RANDOM}, 3, 1, HALF);
        ParseMethodsUI parse_methods = ParseMethodsUI.add_to_dialog(dialog, settings.parse_method_settings());
        return new SplitterUI(localisation_file_field, output_a, output_b, method, parse_methods);
    }

    private void set_localisation_field_visible(boolean value)
    {
        localisation_file_.set_visibility(value);
    }

    public static SplitterDialogSettings create_settings_recorded(GenericDialog dialog)
    {
        String localisation_file = Utils.extract_file_field(dialog);
        String output_a = Utils.extract_file_field(dialog);
        String output_b = Utils.extract_file_field(dialog);
        String method = Utils.extract_radio_buttons(dialog);
        ParseMethod parse_method = ParseMethodsUI.create_settings_recorded(dialog);
        SplitSettings split_settings = SplitSettings.default_();
        split_settings.set_output_1(output_a);
        split_settings.set_output_2(output_b);
        set_split_method(method, split_settings);
        split_settings.set_parse_method(parse_method);
        return SplitterDialogSettings.from(localisation_file, split_settings);
    }

    private static void set_split_method(String method, SplitSettings split_settings)
    {
        switch (method)
        {
            case HALF:
                split_settings.set_half_split();
                break;
            case ZIP:
                split_settings.set_zip_split();
                break;
            case RANDOM:
                split_settings.set_random_split();
                break;
        }
    }

    public SplitterDialogSettings create_settings()
    {
        String localisation_file = localisation_file_.filepath();
        String output_a = output_a_.filepath();
        String output_b = output_b_.filepath();
        String method = method_.get_choice();
        ParseMethod parse_method = parse_methods_.create_settings();
        SplitSettings split_settings = SplitSettings.default_();
        split_settings.set_output_1(output_a);
        split_settings.set_output_2(output_b);
        set_split_method(method, split_settings);
        split_settings.set_parse_method(parse_method);
        return SplitterDialogSettings.from(localisation_file, split_settings);
    }

    public void set_visibility(boolean value)
    {
        localisation_file_.set_visibility(value);
        output_a_.set_visibility(value);
        output_b_.set_visibility(value);
        method_.set_visibility(value);
        parse_methods_.set_visibility(value);
    }

    public void set_localisation_file_visibility(boolean value)
    {
        localisation_file_.set_visibility(value);
    }

}
