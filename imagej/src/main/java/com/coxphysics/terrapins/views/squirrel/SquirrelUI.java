package com.coxphysics.terrapins.views.squirrel;

import com.coxphysics.terrapins.models.squirrel.SquirrelSettings;
import com.coxphysics.terrapins.views.Checkbox;
import com.coxphysics.terrapins.views.NumericField;
import com.coxphysics.terrapins.views.StringChoice;
import com.coxphysics.terrapins.views.Utils;
import ij.gui.GenericDialog;

public class SquirrelUI
{
    private SquirrelUI()
    {

    }
    public static SquirrelUI add_controls_to_dialog(GenericDialog dialog, SquirrelSettings settings)
    {
        String[] image_titles = Utils.get_valid_image_choices(2, new Utils.ImageJImageTitleProvider());
        StringChoice reference_image = Utils.add_string_choice(dialog, "Reference Image", image_titles, image_titles[0]);
        StringChoice sr_image = Utils.add_string_choice(dialog, "Super-Resolution Image", image_titles, image_titles[1]);

        Utils.add_message(dialog, "~~~Processing options~~~");
        NumericField sigma = Utils.add_numeric_field(dialog, "Sigma for smart boundary (nm)", settings.sigma_nm(), 0);
        Checkbox purge = Utils.add_checkbox(dialog, "Check and purge empty frames?", settings.purge_empty_frames());
        Checkbox borders = Utils.add_checkbox(dialog, "Crop black borders from super-resolution image", settings.crop_borders());
        Checkbox registration = Utils.add_checkbox(dialog, "Enable registration", settings.register());
        NumericField misalignment = Utils.add_numeric_field(dialog, "Maximum expected misalignment (0-auto)", settings.misalignment(), 0);

        Utils.add_message(dialog, "~~~Output Image Options ~~~");
        Checkbox crop_and_normalise = Utils.add_checkbox(dialog, "Show intensity-normalised and cropped super-resolution image(s) (default:active)", settings.crop_and_normalise());
        Utils.add_message(dialog, "The above output is required for image fusion");
        Utils.add_message(dialog, " ");

        Checkbox show_rsf_conv = Utils.add_checkbox(dialog, "Show_RSF-convolved super-resolution image(s)", settings.show_rsf_convolved());
        Checkbox show_pos_and_neg = Utils.add_checkbox(dialog, "Show_positive and negative contributions to error map", settings.show_positive_and_negative());
        Utils.add_message(dialog, "If the above checkbox is disabled, error map will just contain absolute values of errors.");
        return new SquirrelUI();
    }
    public SquirrelSettings creaate_settings_recorded(GenericDialog dialog)
    {
        SquirrelSettings settings = SquirrelSettings.default_();

        String reference_image = Utils.extract_string_choice(dialog);
        settings.set_reference_image(reference_image);

        String super_res_image = Utils.extract_string_choice(dialog);
        settings.set_super_res_image(super_res_image);

        int sigma = Utils.extract_numeric_field_as_int(dialog);
        settings.set_sigma_nm(sigma);

        boolean purge = Utils.extract_checkbox_value(dialog);
        settings.set_purge_empty_frames(purge);

        boolean borders = Utils.extract_checkbox_value(dialog);
        settings.set_crop_borders(borders);

        boolean registration = Utils.extract_checkbox_value(dialog);
        settings.set_registration(registration);

        int misalignment = Utils.extract_numeric_field_as_int(dialog);
        settings.set_misalignment(misalignment);

        boolean crop_and_normalise = Utils.extract_checkbox_value(dialog);
        settings.set_crop_and_normalise(crop_and_normalise);

        boolean show_rsf_conv = Utils.extract_checkbox_value(dialog);
        settings.set_show_rsf_convovled(show_rsf_conv);

        boolean show_pos_and_neg = Utils.extract_checkbox_value(dialog);
        settings.set_show_positive_and_negative(show_pos_and_neg);

        return settings;
     }
}
