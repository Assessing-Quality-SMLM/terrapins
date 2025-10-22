package com.coxphysics.terrapins.views.frc;

import com.coxphysics.terrapins.models.frc.ThresholdMethod;
import com.coxphysics.terrapins.views.*;
import com.coxphysics.terrapins.views.renderer.RendererDialogSettings;
import com.coxphysics.terrapins.views.renderer.RendererUI;
import com.coxphysics.terrapins.views.splitter.SplitterDialogSettings;
import com.coxphysics.terrapins.views.splitter.SplitterUI;
import com.coxphysics.terrapins.models.frc.FRCDialogSettings;
import ij.gui.GenericDialog;

import java.awt.event.ItemEvent;

public class FrcUI
{
    private final static String LOCALISATION_FILE = "Localisation file";

    private final static String EXTRA_LOCALISATION_FILE = "Extra localisation file (optional)";

    private final FileField localisation_file_field_;

    private final Checkbox show_splitter_settings_;

    private final Message split_message_;

    private final SplitterUI splitter_ui_;

    private final Checkbox show_renderer_settings_;

    private final Message render_message_;

    private final RendererUI renderer_ui_;

    private final Checkbox custom_data_split_;

    private final FileField extra_localisation_file_;

    private final EnumChoice threshold_method_choice_;

    private final Checkbox use_images_checkbox_;

    private final ImageSelector image_selector_;

    private final FrcDialog dialog_;

    private FrcUI(FrcDialog dialog,
                  FileField localisation_file_field,
                  Checkbox show_splitter_settings,
                  Message split_message,
                  SplitterUI splitter_ui,
                  Checkbox show_render_settings,
                  Message render_message,
                  RendererUI render_ui,
                  Checkbox custom_data_split,
                  FileField extra_localisation_field,
                  Checkbox use_image_checkbox,
                  ImageSelector image_selector,
                  EnumChoice threshold_method_choice)
    {
        dialog_ = dialog;
        localisation_file_field_ = localisation_file_field;
        show_splitter_settings_ = show_splitter_settings;
        split_message_ = split_message;
        splitter_ui_ = splitter_ui;
        show_renderer_settings_ = show_render_settings;
        render_message_ = render_message;
        renderer_ui_ = render_ui;
        custom_data_split_ = custom_data_split;
        extra_localisation_file_ = extra_localisation_field;
        use_images_checkbox_ = use_image_checkbox;
        image_selector_ = image_selector;
        threshold_method_choice_ = threshold_method_choice;
    }

    public static FrcUI add_controls_to_dialog(FrcDialog dialog, FRCDialogSettings settings)
    {
        FileField localisation_field = Utils.add_file_field(dialog, LOCALISATION_FILE, settings.localisation_path());

        Checkbox show_splitter_settings = Utils.add_checkbox(dialog, "Show split settings", false);
        Message split_message = Utils.add_message(dialog, "~~~Split settings~~~");
        split_message.set_visibility(false);
        SplitterUI splitter_ui = SplitterUI.add_controls_to_dialog(dialog, settings.split_settings());
        splitter_ui.set_visibility(false);

        Checkbox custom_data_split = Utils.add_checkbox(dialog, "Custom data split", settings.split_specified());
        FileField extra_localisation_field = Utils.add_file_field(dialog, EXTRA_LOCALISATION_FILE, settings.extra_localisation_path());
        extra_localisation_field.set_visibility(custom_data_split.is_checked());
//
        Checkbox show_render_settings = Utils.add_checkbox(dialog, "Show render settings", false);
        Message render_message = Utils.add_message(dialog, "~~~Render settings~~~");
        render_message.set_visibility(false);
        RendererUI render_ui = RendererUI.add_controls_to_dialog(dialog, settings.render_settings(), false);
        render_ui.set_visibility(false);

        Checkbox use_images_checkbox = Utils.add_checkbox(dialog, "Use Images", settings.use_existing_images());
        ImageSelectorSetttings image_selector_settings = ImageSelectorSetttings.default_();
        image_selector_settings.set_n_images(2);
        image_selector_settings.set_image_names(new String[]{"Image 1", "Image 2"});
        image_selector_settings.set_visible(false);
        ImageSelector image_selector = ImageSelector.add_to_dialog(dialog, image_selector_settings);

        EnumChoice threshold_choice = Utils.add_enum_choice(dialog, "Threshold Method", settings.threshold_method());
        FrcUI frc_ui = new FrcUI(dialog,
                localisation_field,
                show_splitter_settings,
                split_message,
                splitter_ui,
                show_render_settings,
                render_message,
                render_ui,
                custom_data_split,
                extra_localisation_field,
                use_images_checkbox,
                image_selector,
                threshold_choice);
        dialog.addButton("Reset Images", image_selector.reset_images_listener());
        return frc_ui;
    }

    public FRCDialogSettings create_settings_record(GenericDialog dialog)
    {
        FRCDialogSettings settings = FRCDialogSettings.default_();
        String localisation_file = Utils.extract_file_field(dialog);
        settings.set_localisation_file(localisation_file);

        // move dialog along
        boolean show_split_settings = Utils.extract_checkbox_value(dialog);
        SplitterDialogSettings split_settings = SplitterUI.create_settings_recorded(dialog);
        settings.set_split_settings(split_settings.split_settings());

        boolean specify_split = Utils.extract_checkbox_value(dialog);
        settings.set_split_specified(specify_split);

        String extra_file = Utils.extract_file_field(dialog);
        settings.set_extra_localisation_file(extra_file);

        // move dialog along
        boolean show_render_settings = Utils.extract_checkbox_value(dialog);
        RendererDialogSettings render_settings = RendererUI.create_settings_recorded(dialog);
        render_settings.set_parse_method(split_settings.parse_method_settings());
        settings.set_render_settings(render_settings.render_settings());

        boolean use_images = Utils.extract_checkbox_value(dialog);
        settings.set_use_existing_images(use_images);

        String[] image_names = image_selector_.extract_image_names_recorded(dialog);
        settings.set_image_1(image_names[0]);
        settings.set_image_2(image_names[1]);

        ThresholdMethod threshold_method = Utils.extract_enum_choice(dialog, ThresholdMethod.class);
        settings.set_threshold_method(threshold_method);

        return settings;
    }

    public void handle_event(ItemEvent event)
    {
        if (event == null)
            return;
        Object source = event.getSource();
        if (use_images_checkbox_.is_checkbox(source))
        {
            set_image_choice_visibility();
            dialog_.pack();
        }
        else if (custom_data_split_.is_checkbox(source))
        {
            set_extra_localisation_file_visibility();
            dialog_.pack();
        }
        else if(show_splitter_settings_.is_checkbox(source))
        {
            boolean visible = show_splitter_settings_.is_checked();
            split_message_.set_visibility(visible);
            splitter_ui_.set_visibility(visible);
            splitter_ui_.set_localisation_file_visibility(false);
            dialog_.pack();
        }
        else if (show_renderer_settings_.is_checkbox(source))
        {
            boolean visible = show_renderer_settings_.is_checked();
            render_message_.set_visibility(visible);
            renderer_ui_.set_visibility(visible);
            renderer_ui_.set_localisation_file_visibility(false);
            renderer_ui_.set_parse_method_visbility(false);
            dialog_.pack();
        }
        else
        {
            renderer_ui_.handle_event(event);
        }
    }

    private void set_extra_localisation_file_visibility()
    {
        extra_localisation_file_.set_visibility(custom_data_split_.is_checked());
    }

    public void set_image_choice_visibility()
    {
        boolean visibility = use_images_checkbox_.is_checked();
        image_selector_.set_visibility(visibility);
    }

}
