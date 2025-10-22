package com.coxphysics.terrapins.views.renderer;

import com.coxphysics.terrapins.views.*;
import com.coxphysics.terrapins.views.Button;
import com.coxphysics.terrapins.views.Checkbox;
import com.coxphysics.terrapins.views.localisations.ParseMethodsUI;
import com.coxphysics.terrapins.views.psf.PSFDialog;
import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.renderer.RenderSettings;
import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.gui.GenericDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RendererUI
{
    private final static String EXTRA_SETTINGS_NAME_ = "Show extra settings";

    private final static String LOCALISATION_FILE_NAME_ = "Localisation File";

    private final static String OUTPUT_IMAGE_PATH_NAME_ = "Output Image Path (optional)";

    private final static String ZOOM_IMAGE_PATH_NAME_ = "Zoom Image Path (optional)";

    private final static String SIGMA_SCALE = "Rendering Sigma scale";

    private final static String N_THREADS = "Number of Threads to use";

    private final FileField localisation_file_;
    private final FileField output_image_;
    private final FileField zoom_image_;
    private final NumericField camera_pixel_size_nm_;
    private final NumericField magnification_factor_;
    private final StringField global_frame_;
    private final NumericField sigma_scale_;
    private final NumericField n_threads_;
    private final Checkbox extra_settings_;

    private final ParseMethodsUI parse_methods_;

    private final boolean parse_methods_permitted_;

    private final GenericDialog dialog_;

    private RendererUI(FileField localisation_file,
                      FileField output_image,
                      FileField zoom_image,
                      NumericField camera_pixel_size,
                      NumericField magnification_factor,
                      StringField global_frame,
                      NumericField sigma_scale,
                      NumericField n_threads,
                      Checkbox extra_settings,
                      ParseMethodsUI parse_methods,
                      boolean parse_methods_permitted,
                      GenericDialog dialog)
    {
        localisation_file_ = localisation_file;
        output_image_ = output_image;
        zoom_image_ = zoom_image;

        camera_pixel_size_nm_ = camera_pixel_size;
        magnification_factor_ = magnification_factor;
        global_frame_ = global_frame;

        sigma_scale_ = sigma_scale;
        n_threads_ = n_threads;
        extra_settings_ = extra_settings;
        parse_methods_ = parse_methods;
        parse_methods_permitted_ = parse_methods_permitted;

        dialog_ = dialog;
    }
    public static RendererUI add_controls_to_dialog(GenericDialog dialog, RenderSettings settings, boolean parse_methods_permitted)
    {
        RendererDialogSettings dialog_settings = RendererDialogSettings.from(StringUtils.EMPTY_STRING, settings);
        RendererUI ui = add_to_dialog(dialog, dialog_settings, parse_methods_permitted);
        ui.set_localisation_field_visible(false);
        return ui;
    }

    private void set_localisation_field_visible(boolean value)
    {
        localisation_file_.set_visibility(value);
    }

    public static RendererUI add_to_dialog(GenericDialog dialog, RendererDialogSettings settings, boolean parse_methods_permitted)
    {
        FileField localisation_file = Utils.add_file_field(dialog, LOCALISATION_FILE_NAME_, settings.localisation_path());
        FileField output_image = Utils.add_file_field (dialog, OUTPUT_IMAGE_PATH_NAME_, settings.image_path());
        FileField zoom_image = Utils.add_file_field(dialog, ZOOM_IMAGE_PATH_NAME_, settings.zoom_path());

        NumericField camera_pixel_size = Utils.add_numeric_field(dialog, "Camera pixel size (nm)", settings.camera_pixel_size_nm(), 2);
        NumericField magnification_factor = Utils.add_numeric_field(dialog, "Reconstruction Magnification factor", settings.magnification_factor(), 0);
        StringField global_reference_frame = Utils.add_string_field(dialog, "Global frame (start_x, start_y, width, height)", settings.global_frame_of_reference());

        NumericField sigma_scale = Utils.add_numeric_field(dialog, SIGMA_SCALE, settings.sigma_scale(), 0);

        NumericField n_threads = Utils.add_numeric_field(dialog, N_THREADS, settings.n_threads(), 0);
        Checkbox extra_settings = Utils.add_checkbox(dialog,"Show extra settings", false);
        ParseMethodsUI parse_methods = ParseMethodsUI.add_to_dialog(dialog, settings.parse_method_settings());
        parse_methods.set_visibility(false);

        RendererUI ui = new RendererUI(localisation_file,
                                       output_image,
                                       zoom_image,
                                       camera_pixel_size,
                                       magnification_factor,
                                       global_reference_frame,
                                       sigma_scale,
                                       n_threads,
                                       extra_settings,
                                       parse_methods,
                                       parse_methods_permitted,
                                       dialog);
        ui.set_extra_settings_fields_visibility(false);
        return ui;
    }

    public static RendererDialogSettings create_settings_recorded(GenericDialog dialog)
    {
        RendererDialogSettings settings = RendererDialogSettings.default_();

        String localisation_file = Utils.extract_file_field(dialog);
        settings.set_localisation_path(localisation_file);

        String output_image = Utils.extract_file_field(dialog);
        settings.set_image_path(output_image);

        String zoom_image = Utils.extract_file_field(dialog);
        settings.set_zoom_path(zoom_image);

        int camera_pixel_size = Utils.extract_numeric_field_as_int(dialog);
        settings.set_camera_pixel_size(camera_pixel_size);

        int magnification_factor = Utils.extract_numeric_field_as_int(dialog);
        settings.set_magnification_factor(magnification_factor);

        String global_reference_frame = Utils.extract_string_field(dialog);
        settings.set_global_reference_frame(global_reference_frame);

        int sigma_scale = Utils.extract_numeric_field_as_int(dialog);
        settings.set_sigma_scale(sigma_scale);

        int n_threads = Utils.extract_numeric_field_as_int(dialog);
        settings.set_n_threads(n_threads);

        // pull out the extra settings checkbox
        boolean extra_settings = Utils.extract_checkbox_value(dialog);
        ParseMethod parse_method = ParseMethodsUI.create_settings_recorded(dialog);
        settings.set_parse_method(parse_method);

        return settings;
    }

    public void handle_event(AWTEvent e)
    {
        if (is_extra_settings(e.getSource()))
        {
            toggle_extra_settings_visibilty();
        }
    }
    public void set_visibility(boolean value)
    {
        localisation_file_.set_visibility(value);
        camera_pixel_size_nm_.set_visibility(value);
        magnification_factor_.set_visibility(value);
        global_frame_.set_visibility(value);
        sigma_scale_.set_visibility(value);
        extra_settings_.set_visibility(value);
        if(!value)
        {
            set_extra_settings_fields_visibility(false);
        }
        else if(extra_settings_.is_checked())
        {
            set_extra_settings_fields_visibility(true);
        }
        //parse_methods_.set_visibility(value);
    }

    public void set_extra_settings_visibility(boolean value)
    {
        extra_settings_.set_checked(value);
        set_extra_settings_fields_visibility(value);
    }

    private boolean is_extra_settings(Object object)
    {
        return extra_settings_.is_checkbox(object);
    }

    private void toggle_extra_settings_visibilty()
    {
        boolean new_value = extra_settings_.is_checked();
        set_extra_settings_fields_visibility(new_value);
        dialog_.pack();
    }

    private void set_extra_settings_fields_visibility(boolean value)
    {
        output_image_.set_visibility(value);
        zoom_image_.set_visibility(value);
        n_threads_.set_visibility(value);
        if (parse_methods_permitted_)
            parse_methods_.set_visibility(value);
    }

    private void redraw()
    {
        dialog_.pack();
    }

    public void set_localisation_file_visibility(boolean value)
    {
        localisation_file_.set_visibility(value);
    }

    public void set_parse_method_visbility(boolean value)
    {
        parse_methods_.set_visibility(value);
    }

    private static class psf_calculator_action implements ActionListener
    {
        private RendererUI parent_;
        private final RendererDialogSettings settings_;

        private psf_calculator_action(RendererUI parent, RendererDialogSettings settings)
        {
            parent_ = parent;
            settings_ = settings;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            PSFDialog dialog = PSFDialog.create();
            dialog.showDialog();
            if (dialog.wasCanceled())
                return;
            double value = dialog.calculated_psf();
            settings_.set_magnification_factor((int)Math.ceil(value));
            parent_.set_magnification_factor_text_to(settings_.magnification_factor());
            parent_.redraw();
        }

        public void set_ui(RendererUI ui)
        {
            parent_ = ui;
        }
    }

    private void set_magnification_factor_text_to(int value)
    {
        magnification_factor_.set_text(String.valueOf(value));
    }
}
