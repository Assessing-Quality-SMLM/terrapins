package com.coxphysics.terrapins.views.hawkman;

import com.coxphysics.terrapins.models.hawkman.DilateErode;
import com.coxphysics.terrapins.models.hawkman.Settings;
import com.coxphysics.terrapins.views.*;
import com.coxphysics.terrapins.views.psf.PSFDialog;
import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

public class HAWKMANUI
{
    private static final String REFERENCE_IMAGE = "Reference Image (with HAWK)";

    private static final String TEST_IMAGE = "Test Image (No HAWK)";

    private static final String PSF_FWHM = "PSF FWHM in super-res pixels";

    private static final String N_LEVELS = "Number of scales to analyse";

    private static final String EXTRA_SETTINGS = "Extra settings";

    private static final String FWHM_THRESHOLD = "FWHM Threshold";

    private static final String FWHM_SMOOTHING_THRESHOLD = "FWHM Smoothing Threshold";

    private static final String FWHM_OFFSET = "FWHM offset";

    private static final String SKELETONISATION_THRESHOLD = "Skeletonisation Threshold";

    private static final String SKELETONISATION_SMOOTHING_THRESHOLD = "Skeletonisation Smoothing Threshold";

    private static final String SKELETONISATION_OFFSET = "Skeletonisation offset";

    private static final String DILATE_ERODE = "Dilate and Erode";

    private static final String FLATTEN_IMAGES = "Flatten Images";

    private static final String SHOW_BLURRED_SKELETON = "Show blurred skeleton";

    private final GenericDialog dialog_;

    private final NumericField psf_fwhm_;

    private final ImageSelector image_selector_;

    private final Checkbox extra_settings_;

    private final NumericField fwhm_threshold_;

    private final NumericField fwhm_smoothing_threshold_;
    private final NumericField fwhm_offset_;
    private final NumericField skel_threshold_;
    private final NumericField skel_smoothing_threshold_;
    private final NumericField skel_offset_;
    private final Checkbox dialate_and_erode_;
    private final Checkbox flatten_images_;
    private final Checkbox show_blurred_skeleton_;

    private HAWKMANUI(GenericDialog dialog,
                      NumericField psf_fwhm,
                      ImageSelector image_selector, Checkbox extra_settings,
                      NumericField fwhm_threshold,
                      NumericField fwhm_smoothing_threshold,
                      NumericField fwhm_offset,
                      NumericField skel_threshold,
                      NumericField skel_smoothing_threshold,
                      NumericField skel_offset,
                      Checkbox dialate_and_erode,
                      Checkbox flatten_images,
                      Checkbox show_blurred_skeleton)
    {
        dialog_ = dialog;
        psf_fwhm_ = psf_fwhm;
        image_selector_ = image_selector;
        extra_settings_ = extra_settings;
        fwhm_threshold_ = fwhm_threshold;
        fwhm_smoothing_threshold_ = fwhm_smoothing_threshold;
        fwhm_offset_ = fwhm_offset;
        skel_threshold_ = skel_threshold;
        skel_smoothing_threshold_ = skel_smoothing_threshold;
        skel_offset_ = skel_offset;
        dialate_and_erode_ = dialate_and_erode;
        flatten_images_ = flatten_images;
        show_blurred_skeleton_ = show_blurred_skeleton;
    }

    public static HAWKMANUI add_controls_to_dialog(GenericDialog dialog, Settings settings)
    {
        ImageSelectorSetttings image_selector_settings = ImageSelectorSetttings.default_();
        image_selector_settings.set_image_names(new String[]{REFERENCE_IMAGE, TEST_IMAGE});
        ImageSelector image_selector = ImageSelector.add_to_dialog(dialog, image_selector_settings);

        NumericField psf_fwhm = Utils.add_numeric_field(dialog, PSF_FWHM, settings.psf_size(), 0);
        NumericField n_levels = Utils.add_numeric_field(dialog, N_LEVELS, settings.max_scale(), 0);

        Checkbox extra_settings = Utils.add_checkbox(dialog, EXTRA_SETTINGS, false);

        NumericField fwhm_threshold = Utils.add_numeric_field(dialog, FWHM_THRESHOLD, settings.fwhm_threshold(), 3);
        NumericField fwhm_smoothing_threshold = Utils.add_numeric_field(dialog, FWHM_SMOOTHING_THRESHOLD, settings.fwhm_smoothing(), 3);
        NumericField fwhm_offset = Utils.add_numeric_field(dialog, FWHM_OFFSET, settings.fwhm_offset(), 3);

        NumericField skel_threshold = Utils.add_numeric_field(dialog, SKELETONISATION_THRESHOLD, settings.skeletonise_threshold(), 3);
        NumericField skel_smoothing_threshold = Utils.add_numeric_field(dialog, SKELETONISATION_SMOOTHING_THRESHOLD, settings.skeletonise_smoothing(), 3);
        NumericField skel_offset = Utils.add_numeric_field(dialog, SKELETONISATION_OFFSET, settings.skeletonise_offset(), 3);

        Checkbox dialate_and_erode = Utils.add_checkbox(dialog, DILATE_ERODE, settings.dilate_erode_method() == DilateErode.Method.B);
        Checkbox flatten_images = Utils.add_checkbox(dialog, FLATTEN_IMAGES, settings.flatten_images());
        Checkbox show_blurred_skeleton = Utils.add_checkbox(dialog, SHOW_BLURRED_SKELETON, settings.blur_skeletons());

        Button reset_images = Utils.add_button(dialog, "Reset Images", image_selector.reset_images_listener());

        psf_button_listener psf_button_listener = new psf_button_listener();
        Button psf_calculator = Utils.add_button(dialog, "Psf Caculator", psf_button_listener);

        HAWKMANUI ui = new HAWKMANUI(dialog,
                             psf_fwhm,
                             image_selector,
                             extra_settings,
                             fwhm_threshold,
                             fwhm_smoothing_threshold,
                             fwhm_offset,
                             skel_threshold,
                             skel_smoothing_threshold,
                             skel_offset,
                             dialate_and_erode,
                             flatten_images,
                             show_blurred_skeleton);
        ui.set_extra_settings_visibility(extra_settings.is_checked());
        psf_button_listener.set_ui(ui);
        return ui;
    }

    public Settings create_settings_recorded(GenericDialog dialog)
    {
        Settings settings = Settings.default_();
        String[] image_names = image_selector_.extract_image_names_recorded(dialog);
        settings.set_ref_name(image_names[0]);
        settings.set_test_name(image_names[1]);

//        String reference_image = Utils.extract_file_field(dialog);
//        settings.set_ref_name(reference_image);
//
//        String test_image = Utils.extract_file_field(dialog);
//        settings.set_test_name(test_image);

        int psf_fwhm = Utils.extract_numeric_field_as_int(dialog);
        settings.set_psf_size(psf_fwhm);

        int n_levels = Utils.extract_numeric_field_as_int(dialog);
        settings.set_max_scale(n_levels);

        // need to move the dialog counters on
        boolean extra_settings = Utils.extract_checkbox_value(dialog);

        double fwhm_threshold = Utils.extract_numeric_field(dialog);
        settings.set_fwhm_threshold(fwhm_threshold);

        double fwhm_smoothing_threshold = Utils.extract_numeric_field(dialog);
        settings.set_fwhm_smoothing(fwhm_smoothing_threshold);

        double fwhm_offset = Utils.extract_numeric_field(dialog);
        settings.set_fwhm_offset(fwhm_offset);

        double skel_threshold = Utils.extract_numeric_field(dialog);
        settings.set_skeletonisation_threshold(skel_threshold);

        double skel_smoothing_threshold = Utils.extract_numeric_field(dialog);
        settings.set_skeletonisation_smoothing(skel_smoothing_threshold);

        double skel_offset = Utils.extract_numeric_field(dialog);
        settings.set_skeletonisation_offset(skel_offset);

        boolean dialate_and_erode = Utils.extract_checkbox_value(dialog);
        DilateErode.Method method = dialate_and_erode ? DilateErode.Method.B : DilateErode.Method.None;
        settings.set_dilate_erode_method(method);

        boolean flatten_images = Utils.extract_checkbox_value(dialog);
        settings.set_flatten_images(flatten_images);

        boolean show_blurred_skeleton = Utils.extract_checkbox_value(dialog);
        settings.set_blur_skeleton(show_blurred_skeleton);

        return settings;
    }

    public void handle_event(ItemEvent event)
    {
        if (extra_settings_.is_checkbox(event.getSource()))
        {
            toggle_extra_settings_visibility();
        }
    }

    private void toggle_extra_settings_visibility()
    {
        boolean value = extra_settings_.is_checked();
        set_extra_settings_visibility(value);
        dialog_.pack();
    }

    private void set_extra_settings_visibility(boolean value)
    {
        fwhm_threshold_.set_visibility(value);
        fwhm_smoothing_threshold_.set_visibility(value);
        fwhm_offset_.set_visibility(value);
        skel_threshold_.set_visibility(value);
        skel_smoothing_threshold_.set_visibility(value);
        skel_offset_.set_visibility(value);
        dialate_and_erode_.set_visibility(value);
        flatten_images_.set_visibility(value);
        show_blurred_skeleton_.set_visibility(value);
    }

    private static class psf_button_listener implements ActionListener
    {
        private HAWKMANUI ui_ = null;

        private psf_button_listener()
        {

        }

        public void set_ui(HAWKMANUI ui)
        {
            ui_ = ui;
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            PSFDialog dialog = PSFDialog.create();
            dialog.showDialog();
            if (dialog.wasCanceled())
                return;
            double value = dialog.calculated_psf();
            int value_to_int = (int)Math.ceil(value);
            ui_.psf_fwhm_.set_text(String.valueOf(value_to_int));;
            ui_.dialog_.pack();
        }
    }
}
