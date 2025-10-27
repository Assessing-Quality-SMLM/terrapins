package com.coxphysics.terrapins.views.hawk;

import com.coxphysics.terrapins.models.hawk.Settings;
import com.coxphysics.terrapins.views.NumericField;
import com.coxphysics.terrapins.views.StringChoice;
import com.coxphysics.terrapins.views.TextAreas;
import com.coxphysics.terrapins.views.Utils;
import com.coxphysics.terrapins.models.hawk.Config;
import ij.gui.GenericDialog;
import org.jetbrains.annotations.NotNull;


import static com.coxphysics.terrapins.models.hawk.ConstantsKt.*;

public class HawkUI
{
    private final NumericField n_levels_;
    private final StringChoice negative_values_;
    private final StringChoice output_order_;

    private final TextAreas text_;

    private HawkUI(NumericField n_levels, StringChoice negative_values, StringChoice output_order, TextAreas text)
    {
        n_levels_ = n_levels;
        output_order_ = output_order;
        negative_values_ = negative_values;
        text_ = text;
    }

    public static HawkUI add_to_dialog(GenericDialog dialog, Settings settings)
    {
        NumericField n_levels = Utils.add_numeric_field(dialog,"Number of Levels", settings.n_levels(), 0);
        StringChoice negative_values = Utils.add_string_choice(dialog, "Negative values", new String[]{ABSOLUTE, SEPARATE}, settings.negative_handling());
        StringChoice output_order = Utils.add_string_choice(dialog,"Output order", new String[]{SEQUENTIAL, TEMPORALLY}, settings.output_style());
        TextAreas text = Utils.add_text_areas(dialog, "", null, 8, 30);
        return new HawkUI(n_levels, negative_values, output_order, text);
    }

    public static Config create_config(GenericDialog dialog)
    {
        int n_levels = Utils.extract_numeric_field_as_int(dialog);
        String negative_handling = Utils.extract_string_choice(dialog);
        String output_style = Utils.extract_string_choice(dialog);

        Settings settings = Settings.from(n_levels, negative_handling, output_style);
        return Config.from(settings);
    }

    public Config read_config()
    {
        Settings settings = read_into_settings();
        return Config.from(settings);
    }

    @NotNull
    private Settings read_into_settings()
    {
        return Settings.from(n_levels(), negative_handling(), output_style());
    }

    public int n_levels()
    {
        return n_levels_.get_value();
    }

    private String negative_handling()
    {
        return negative_values_.choice();
    }

    private String output_style()
    {
        return output_order_.choice();
    }


    public void set_visibility(boolean value)
    {
        n_levels_.set_visibility(value);
        negative_values_.set_visible(value);
        output_order_.set_visible(value);
        text_.set_visibility(value);
    }
}
