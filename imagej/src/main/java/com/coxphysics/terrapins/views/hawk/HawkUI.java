package com.coxphysics.terrapins.views.hawk;

import com.coxphysics.terrapins.models.hawk.Config;
import com.coxphysics.terrapins.models.hawk.Settings;
import com.coxphysics.terrapins.views.*;
import ij.gui.GenericDialog;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static com.coxphysics.terrapins.models.hawk.ConstantsKt.*;

public class HawkUI implements ItemListener {
    private final NumericField n_levels_;
    private final StringChoice negative_values_;
    private final StringChoice output_order_;

    private final TextAreas text_;

    private final Checkbox save_to_disk_;
    private final FileField output_filename_;
    private HawkUI(
            NumericField n_levels,
            StringChoice negative_values,
            StringChoice output_order,
            TextAreas text,
            Checkbox save_to_disk,
            FileField output_filename)
    {
        n_levels_ = n_levels;
        output_order_ = output_order;
        negative_values_ = negative_values;
        text_ = text;
        save_to_disk_ = save_to_disk;
        output_filename_ = output_filename;
    }

    public static HawkUI add_to_dialog(GenericDialog dialog, Settings settings)
    {
        NumericField n_levels = Utils.add_numeric_field(dialog,"Number of Levels", settings.n_levels(), 0);
        StringChoice negative_values = Utils.add_string_choice(dialog, "Negative values", new String[]{ABSOLUTE, SEPARATE}, settings.negative_handling());
        StringChoice output_order = Utils.add_string_choice(dialog,"Output order", new String[]{SEQUENTIAL, TEMPORALLY}, settings.output_style());
        TextAreas text = Utils.add_text_areas(dialog, "", null, 8, 30);
        Checkbox save_to_disk = Utils.add_checkbox(dialog, "Save to disk", false);
        FileField file_field = Utils.add_file_field(dialog, "HAWK output file", settings.filename());
        file_field.set_enabled(save_to_disk.is_checked());
        HawkUI ui = new HawkUI(n_levels, negative_values, output_order, text, save_to_disk, file_field);
        save_to_disk.add_item_listener(ui);
        return ui;
    }

    public Config create_settings_recorded(GenericDialog dialog)
    {
        return create_config(dialog);
    }

    public static Config create_config(GenericDialog dialog)
    {
        int n_levels = Utils.extract_numeric_field_as_int(dialog);
        String negative_handling = Utils.extract_string_choice(dialog);
        String output_style = Utils.extract_string_choice(dialog);

        boolean save_to_disk = Utils.extract_checkbox_value(dialog);
        String filename = Utils.extract_file_field(dialog);

        Settings settings = Settings.from(n_levels, negative_handling, output_style);
        if (save_to_disk)
            settings.set_filename(filename);
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
        String filename = output_filename_.filepath();
        Settings settings = Settings.from(n_levels(), negative_handling(), output_style());
        if (save_to_disk_.is_checked())
            settings.set_filename(filename);
        return settings;
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
        save_to_disk_.set_visibility(value);
        output_filename_.set_visibility(value);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        Object source = e.getSource();
        if (save_to_disk_.is_checkbox(source))
        {
            sync_filename_to_checkbox();
        }
    }

    private void sync_filename_to_checkbox()
    {
        output_filename_.set_enabled(save_to_disk_.is_checked());
    }

    public void save_to_disk(boolean value)
    {
        save_to_disk_.set_checked(value);
        sync_filename_to_checkbox();
    }
}
