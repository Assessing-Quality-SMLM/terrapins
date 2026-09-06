package com.coxphysics.terrapins.views.oneclick;

import com.coxphysics.terrapins.models.utils.ActionableDocumentListener;
import com.coxphysics.terrapins.models.utils.ActionableListener;
import com.coxphysics.terrapins.view_models.oneclick.OneClickVM;

import javax.swing.*;
import java.awt.*;

/**
 * The one-click tab: a raw stack, four numbers about the microscope, and Run.
 * <p>
 * Written by hand rather than with the GUI designer, unlike the other views here. The layout is a
 * single column of labelled fields, which is quicker to read as code than as generated
 * {@code $$$setupUI$$$}, and it keeps the whole of the front door - the fields, why each is
 * needed, and what happens when one is missing - in one reviewable file. The designer earns its
 * place on the denser workflow panels; it does not on this one.
 * <p>
 * Everything optional is marked as optional in the label. The camera gain in particular is easy
 * to mistake for a required calibration: it only affects the predicted localisation precision and
 * cannot move a localisation, so leaving it blank costs one report line rather than the run.
 */
public class OneClickView extends JPanel
{
    private final JComboBox<String> image_box_ = new JComboBox<>();
    private final JButton reset_images_btn_ = new JButton("Reset image list");
    private final JComboBox<String> fitter_box_ = new JComboBox<>();
    private final JTextField pixel_size_field_ = new JTextField();
    private final JTextField psf_fwhm_field_ = new JTextField();
    private final JTextField magnification_field_ = new JTextField();
    private final JTextField hawk_levels_field_ = new JTextField();
    private final JTextField gain_field_ = new JTextField();
    private final JCheckBox emccd_box_ = new JCheckBox("EMCCD camera");
    private final JLabel status_label_ = new JLabel(" ");
    private final JButton run_btn_ = new JButton("Run");

    private OneClickVM view_model_;
    /** Suppresses the field listeners while the view is being populated from the model. */
    private boolean loading_ = false;

    public OneClickView()
    {
        setLayout(new GridBagLayout());
        build();
    }

    public static OneClickView from(OneClickVM view_model)
    {
        OneClickView view = new OneClickView();
        view.set_view_model(view_model);
        return view;
    }

    public void set_view_model(OneClickVM view_model)
    {
        view_model_ = view_model;
        reload();
    }

    private void build()
    {
        int row = 0;
        add_heading("Raw data", row++);
        add_row("Image stack:", image_box_, row++,
                "The raw camera frames. Everything else is derived from these.");
        add_component(reset_images_btn_, row++);

        add_heading("Microscope", row++);
        add_row("Camera pixel size (nm):", pixel_size_field_, row++,
                "Pixel size projected into the sample, not the physical sensor pitch.");
        add_row("PSF FWHM (nm):", psf_fwhm_field_, row++,
                "Typically 230-280 nm depending on wavelength and NA.");
        add_row("Render magnification:", magnification_field_, row++,
                "How finely the reconstruction is sampled. A display choice, not a resolution.");

        add_heading("Analysis", row++);
        add_row("Fitter:", fitter_box_, row++, "Which localiser to run on the raw frames.");
        add_row("HAWK levels:", hawk_levels_field_, row++,
                "Reduced automatically if the stack is too short to support this many.");
        add_row("Camera gain (photons/ADU, optional):", gain_field_, row++,
                "Only used to predict localisation precision; it cannot move a localisation.");
        add_component(emccd_box_, row++);

        status_label_.setForeground(Color.GRAY);
        add_component(status_label_, row++);
        add_component(run_btn_, row++);

        // Push everything to the top rather than spreading it down a tall window.
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = row;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.VERTICAL;
        add(Box.createVerticalGlue(), filler);

        wire();
    }

    private void add_heading(String text, int row)
    {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        GridBagConstraints c = base_constraints(row);
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(12, 4, 2, 4);
        add(label, c);
    }

    private void add_row(String label_text, JComponent field, int row, String tooltip)
    {
        JLabel label = new JLabel(label_text);
        label.setToolTipText(tooltip);
        field.setToolTipText(tooltip);

        GridBagConstraints label_c = base_constraints(row);
        label_c.gridx = 0;
        label_c.weightx = 0.0;
        add(label, label_c);

        GridBagConstraints field_c = base_constraints(row);
        field_c.gridx = 1;
        field_c.weightx = 1.0;
        add(field, field_c);
    }

    private void add_component(JComponent component, int row)
    {
        GridBagConstraints c = base_constraints(row);
        c.gridx = 0;
        c.gridwidth = 2;
        add(component, c);
    }

    private GridBagConstraints base_constraints(int row)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }

    private void wire()
    {
        image_box_.addActionListener(ActionableListener.from(this, OneClickView::on_image_changed));
        reset_images_btn_.addActionListener(
                ActionableListener.from(this, OneClickView::reload_image_list));
        fitter_box_.addActionListener(ActionableListener.from(this, OneClickView::on_fitter_changed));
        emccd_box_.addActionListener(ActionableListener.from(this, OneClickView::on_emccd_changed));
        run_btn_.addActionListener(ActionableListener.from(this, OneClickView::run));

        pixel_size_field_.getDocument().addDocumentListener(
                ActionableDocumentListener.from(this, OneClickView::on_pixel_size_changed));
        psf_fwhm_field_.getDocument().addDocumentListener(
                ActionableDocumentListener.from(this, OneClickView::on_psf_fwhm_changed));
        magnification_field_.getDocument().addDocumentListener(
                ActionableDocumentListener.from(this, OneClickView::on_magnification_changed));
        hawk_levels_field_.getDocument().addDocumentListener(
                ActionableDocumentListener.from(this, OneClickView::on_hawk_levels_changed));
        gain_field_.getDocument().addDocumentListener(
                ActionableDocumentListener.from(this, OneClickView::on_gain_changed));
    }

    /** Repopulates every control from the model, without the listeners writing back as it goes. */
    private void reload()
    {
        if (view_model_ == null)
        {
            return;
        }
        loading_ = true;
        try
        {
            reload_image_list();
            fitter_box_.removeAllItems();
            for (String name : view_model_.fitter_names())
            {
                fitter_box_.addItem(name);
            }
            pixel_size_field_.setText(Double.toString(view_model_.camera_pixel_size_nm()));
            psf_fwhm_field_.setText(Double.toString(view_model_.psf_fwhm_nm()));
            magnification_field_.setText(Double.toString(view_model_.magnification()));
            hawk_levels_field_.setText(Integer.toString(view_model_.hawk_levels()));
            gain_field_.setText(view_model_.has_photons_per_adu()
                    ? Double.toString(view_model_.photons_per_adu()) : "");
            emccd_box_.setSelected(view_model_.emccd());
        }
        finally
        {
            loading_ = false;
        }
        refresh_status();
    }

    /**
     * Re-reads the open images. ImageJ has no event for a window opening, so a user who opens
     * their data after this tab was built needs a way to say so - the same reason the images
     * workflow has this button.
     */
    private void reload_image_list()
    {
        if (view_model_ == null)
        {
            return;
        }
        boolean was_loading = loading_;
        loading_ = true;
        try
        {
            image_box_.removeAllItems();
            String[] titles = view_model_.image_titles();
            if (titles != null)
            {
                for (String title : titles)
                {
                    image_box_.addItem(title);
                }
            }
        }
        finally
        {
            loading_ = was_loading;
        }
        if (!loading_)
        {
            on_image_changed();
        }
    }

    private void on_image_changed()
    {
        if (loading_ || view_model_ == null)
        {
            return;
        }
        int index = image_box_.getSelectedIndex();
        if (index >= 0)
        {
            view_model_.set_image_index(index);
        }
        refresh_status();
    }

    private void on_fitter_changed()
    {
        if (loading_ || view_model_ == null)
        {
            return;
        }
        view_model_.set_fitter_index(Math.max(0, fitter_box_.getSelectedIndex()));
        refresh_status();
    }

    private void on_emccd_changed()
    {
        if (loading_ || view_model_ == null)
        {
            return;
        }
        view_model_.set_emccd(emccd_box_.isSelected());
    }

    private void on_pixel_size_changed()
    {
        apply(pixel_size_field_, view_model_.set_camera_pixel_size_nm(pixel_size_field_.getText()));
    }

    private void on_psf_fwhm_changed()
    {
        apply(psf_fwhm_field_, view_model_.set_psf_fwhm_nm(psf_fwhm_field_.getText()));
    }

    private void on_magnification_changed()
    {
        apply(magnification_field_, view_model_.set_magnification(magnification_field_.getText()));
    }

    private void on_hawk_levels_changed()
    {
        apply(hawk_levels_field_, view_model_.set_hawk_levels(hawk_levels_field_.getText()));
    }

    private void on_gain_changed()
    {
        apply(gain_field_, view_model_.set_photons_per_adu(gain_field_.getText()));
    }

    private void apply(JTextField field, Color background)
    {
        if (loading_ || view_model_ == null)
        {
            return;
        }
        field.setBackground(background);
        refresh_status();
    }

    /**
     * Keeps the status line and the Run button honest about whether the settings can be run, and
     * says what is missing rather than only disabling the button.
     */
    private void refresh_status()
    {
        if (view_model_ == null)
        {
            return;
        }
        String error = view_model_.error_string();
        boolean runnable = error == null;
        run_btn_.setEnabled(runnable);
        status_label_.setText(runnable ? view_model_.precision_note() : error);
        status_label_.setForeground(runnable ? Color.GRAY : Color.RED);
    }

    private void run()
    {
        if (view_model_ == null || !view_model_.is_runnable())
        {
            return;
        }
        view_model_.run();
    }
}
