package com.coxphysics.terrapins.views.hawk;

import com.coxphysics.terrapins.models.hawk.NegativeValuesPolicy;
import com.coxphysics.terrapins.models.hawk.OutputStyle;
import com.coxphysics.terrapins.view_models.hawk.HAWKVM;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ij.ImagePlus;
import ij.WindowManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class ImageNameListener implements ItemListener
{
    private final HAWKView view_;
    private ImageNameListener(HAWKView view)
    {
        view_ = view;
    }

    public static ImageNameListener from(HAWKView view)
    {
        return new ImageNameListener(view);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e == null)
            return;
        view_.extract_image();
    }
}

class OutputStyleListener implements ItemListener
{
    private final HAWKView view_;

    private OutputStyleListener(HAWKView view)
    {
        view_ = view;
    }

    public static OutputStyleListener from(HAWKView view)
    {
        return new OutputStyleListener(view);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e == null)
            return;
        view_.extract_output_style();
    }
}

class NegativeValuePolicyListener implements ItemListener
{
    private final HAWKView view_;

    private NegativeValuePolicyListener(HAWKView view)
    {
        view_ = view;
    }

    public static NegativeValuePolicyListener from(HAWKView view)
    {
        return new NegativeValuePolicyListener(view);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e == null)
            return;
        view_.extract_negative_value_policy();
    }
}

class NLevelsListener implements DocumentListener
{
    private final HAWKView view_;

    private NLevelsListener(HAWKView view)
    {
        view_ = view;
    }

    public static NLevelsListener from(HAWKView view)
    {
        return new NLevelsListener(view);
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        view_.update_n_levels_value();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        view_.update_n_levels_value();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        view_.update_n_levels_value();
    }
}

class RunListener implements ActionListener
{
   private final HAWKView view_;

    private RunListener(HAWKView view)
    {
        view_ = view;
    }

    public static RunListener from(HAWKView view)
    {
        return new RunListener(view);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        view_.close_ok();
    }
}

class CancelListener implements ActionListener
{
   private final HAWKView view_;

    private CancelListener(HAWKView view)
    {
        view_ = view;
    }

    public static CancelListener from(HAWKView view)
    {
        return new CancelListener(view);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        view_.close_cancel();
    }
}

public class HAWKView extends JDialog {
    private JComboBox<String> image_name_combo_box_;
    private JLabel image_label_;
    private JComboBox<OutputStyle> output_order_combo_box_;
    private JComboBox<NegativeValuesPolicy> negative_values_combo_box_;
    private JLabel negative_values_label_;
    private JButton run_btn_;
    private JButton cancel_btn_;
    private JPanel content_panel_;
    private JLabel n_levels_label_;
    private JTextField n_levels_field_;

    private HAWKVM view_model_;

    private boolean ok_ = false;

    public HAWKView()
    {
        super((Dialog) null, "HAWK", true);
        add(content_panel_);
        image_name_combo_box_.addItemListener(ImageNameListener.from(this));
        output_order_combo_box_.addItemListener(OutputStyleListener.from(this));
        negative_values_combo_box_.addItemListener(NegativeValuePolicyListener.from(this));
        n_levels_field_.getDocument().addDocumentListener(NLevelsListener.from(this));
        run_btn_.addActionListener(RunListener.from(this));
        cancel_btn_.addActionListener(CancelListener.from(this));
    }

    public static HAWKView from(HAWKVM view_model) {
        HAWKView view = new HAWKView();
        view_model.set_n_levels_default_colour(view.n_levels_field_.getBackground());
        view.set_view_model(view_model);
        return view;
    }

    public void set_view_model(HAWKVM view_model)
    {
        view_model_ = view_model;
        draw();
    }

    public boolean ok()
    {
        return ok_;
    }

    public void update_n_levels_value()
    {
        boolean ok = view_model_.set_n_levels(n_levels_field_.getText());
        if (!ok)
            n_levels_field_.setBackground(view_model_.n_levels_error_colour());
        else
        {
            n_levels_field_.setBackground(view_model_.n_levels_colour());
        }
    }

    public void extract_image() {
        int selected_image = image_name_combo_box_.getSelectedIndex();
        int id = WindowManager.getNthImageID(selected_image + 1);
        ImagePlus image = WindowManager.getImage(id);
        view_model_.set_image(image);
    }

    public void extract_output_style() {
        OutputStyle selected_style = (OutputStyle) output_order_combo_box_.getSelectedItem();
        if (selected_style != null)
            view_model_.set_output_style(selected_style);
    }

    public void extract_negative_value_policy() {
        NegativeValuesPolicy selected_style = (NegativeValuesPolicy) negative_values_combo_box_.getSelectedItem();
        if (selected_style != null)
            view_model_.set_negative_value_policy(selected_style);
    }

    private void draw() {
        draw_image_names();
        draw_n_levels();
        draw_output_style_options();
        draw_negative_value_options();
    }

    private void draw_image_names() {
        for (String value : WindowManager.getImageTitles()) {
            image_name_combo_box_.addItem(value);
        }
    }

    private void draw_n_levels() {
        String value = Integer.toString(view_model_.n_levels());
        n_levels_field_.setText(value);
    }

    private void draw_negative_value_options() {
        for (NegativeValuesPolicy value : NegativeValuesPolicy.getEntries()) {
            negative_values_combo_box_.addItem(value);
        }
    }

    private void draw_output_style_options() {
        for (OutputStyle value : OutputStyle.getEntries()) {
            output_order_combo_box_.addItem(value);
        }
    }

    public void close_ok()
    {
        ok_ = true;
        dispose();
    }

    public void close_cancel()
    {
        ok_ = false;
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        content_panel_ = new JPanel();
        content_panel_.setLayout(new GridLayoutManager(7, 3, new Insets(5, 5, 5, 5), -1, -1));
        image_name_combo_box_ = new JComboBox();
        content_panel_.add(image_name_combo_box_, new GridConstraints(0, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        image_label_ = new JLabel();
        image_label_.setText("Image");
        content_panel_.add(image_label_, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Output Order");
        content_panel_.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        output_order_combo_box_ = new JComboBox();
        content_panel_.add(output_order_combo_box_, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negative_values_label_ = new JLabel();
        negative_values_label_.setText("Negative Values");
        content_panel_.add(negative_values_label_, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negative_values_combo_box_ = new JComboBox();
        content_panel_.add(negative_values_combo_box_, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        run_btn_ = new JButton();
        run_btn_.setText("Apply");
        content_panel_.add(run_btn_, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancel_btn_ = new JButton();
        cancel_btn_.setText("Cancel");
        content_panel_.add(cancel_btn_, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        content_panel_.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        n_levels_label_ = new JLabel();
        n_levels_label_.setText("Number of levels");
        content_panel_.add(n_levels_label_, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        n_levels_field_ = new JTextField();
        content_panel_.add(n_levels_field_, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return content_panel_;
    }

}
