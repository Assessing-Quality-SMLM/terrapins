package com.coxphysics.terrapins.views.hawk;

import com.coxphysics.terrapins.models.hawk.NegativeValuesPolicy;
import com.coxphysics.terrapins.models.hawk.OutputStyle;
import com.coxphysics.terrapins.models.utils.ActionableListener;
import com.coxphysics.terrapins.view_models.hawk.HAWKVM;
import com.coxphysics.terrapins.views.HAWKWorker;
import com.coxphysics.terrapins.views.TERRAPINS.ImageSelectorView;
import com.coxphysics.terrapins.views.TERRAPINS.PathSelectorView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import kotlin.Unit;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


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
    private JComboBox<OutputStyle> output_order_combo_box_;
    private JComboBox<NegativeValuesPolicy> negative_values_combo_box_;
    private JLabel negative_values_label_;
    private JButton run_btn_;
    private JButton cancel_btn_;
    private JPanel content_panel_;
    private JLabel n_levels_label_;
    private JTextField n_levels_field_;
    private ImageSelectorView image_selector_ctrl_;
    private JButton save_to_disk_btn_;
    private JPanel save_to_disk_panel_;
    private PathSelectorView path_selector_view_;

    private HAWKVM view_model_ = HAWKVM.default_();

    private boolean ok_ = false;

    public HAWKView() {
        super((Dialog) null, "HAWK", true);
        view_model_.set_n_levels_default_colour(n_levels_field_.getBackground());
        add(content_panel_);
        output_order_combo_box_.addItemListener(OutputStyleListener.from(this));
        negative_values_combo_box_.addItemListener(NegativeValuePolicyListener.from(this));
        n_levels_field_.getDocument().addDocumentListener(NLevelsListener.from(this));
        run_btn_.addActionListener(ActionableListener.from(this, HAWKView::run_filter));
        cancel_btn_.addActionListener(CancelListener.from(this));
        save_to_disk_btn_.addActionListener(ActionableListener.from(this, HAWKView::save_to_disk));
    }

    public static HAWKView from(HAWKVM view_model) {
        HAWKView view = new HAWKView();
        view.set_view_model(view_model);
        return view;
    }

    public void set_view_model(HAWKVM view_model) {
        Color default_bg_colour = view_model_.n_levels_default_colour();
        view_model_ = view_model;
        view_model_.set_n_levels_default_colour(default_bg_colour);
        image_selector_ctrl_.set_view_model(view_model_.image_selector_vm());
        path_selector_view_.set_view_model(view_model.output_file_vm());
        draw();
    }

    public boolean ok() {
        return ok_;
    }

    public void update_n_levels_value() {
        String text = n_levels_field_.getText();
        boolean ok = view_model_.set_n_levels(text);
        set_n_levels_background_colour(ok);
    }

    private void set_n_levels_background_colour(boolean ok) {
        Color background_colour = ok ? view_model_.n_levels_colour() : view_model_.n_levels_error_colour();
        n_levels_field_.setBackground(background_colour);
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
        image_selector_ctrl_.draw();
        draw_n_levels();
        draw_output_style_options();
        draw_negative_value_options();
    }

    private void draw_n_levels() {
        String value = Integer.toString(view_model_.n_levels());
        n_levels_field_.setText(value);
        set_n_levels_background_colour(true);
    }

    private void draw_negative_value_options() {
        negative_values_combo_box_.removeAllItems();
        for (NegativeValuesPolicy value : NegativeValuesPolicy.getEntries()) {
            negative_values_combo_box_.addItem(value);
        }
    }

    private void draw_output_style_options() {
        output_order_combo_box_.removeAllItems();
        for (OutputStyle value : OutputStyle.getEntries()) {
            output_order_combo_box_.addItem(value);
        }
    }

    private void propogate_image_selection() {
        view_model_.propogate_image_selection();
    }

    public void run_filter()
    {
        view_model_.run_filter();
        close_ok();
    }

    public void close_ok() {
        propogate_image_selection();
        ok_ = true;
        dispose();
    }

    public void close_cancel() {
        propogate_image_selection();
        ok_ = false;
        dispose();
    }

    public void save_to_disk() {
        save_to_disk_btn_.setEnabled(false);
        path_selector_view_.set_enabled(false);
        view_model_.save_to_disk(ok -> {
            update_save_button_background(ok);
            return Unit.INSTANCE;
        });
    }

    private void update_save_button_background(boolean ok)
    {
        save_to_disk_btn_.setEnabled(true);
        path_selector_view_.set_enabled(true);
        if (ok)
        {
            save_to_disk_btn_.setBackground(new JButton().getBackground());
            path_selector_view_.update_data_path_from_view();
        }
        else
        {
            save_to_disk_btn_.setBackground(Color.RED);
        }
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
        final JLabel label1 = new JLabel();
        label1.setText("Output Order");
        content_panel_.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        output_order_combo_box_ = new JComboBox();
        content_panel_.add(output_order_combo_box_, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negative_values_label_ = new JLabel();
        negative_values_label_.setText("Negative Values");
        content_panel_.add(negative_values_label_, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negative_values_combo_box_ = new JComboBox();
        content_panel_.add(negative_values_combo_box_, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        run_btn_ = new JButton();
        run_btn_.setText("Apply");
        content_panel_.add(run_btn_, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancel_btn_ = new JButton();
        cancel_btn_.setText("Cancel");
        content_panel_.add(cancel_btn_, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        content_panel_.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        n_levels_label_ = new JLabel();
        n_levels_label_.setText("Number of levels");
        content_panel_.add(n_levels_label_, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        n_levels_field_ = new JTextField();
        content_panel_.add(n_levels_field_, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        image_selector_ctrl_ = new ImageSelectorView();
        content_panel_.add(image_selector_ctrl_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        save_to_disk_panel_ = new JPanel();
        save_to_disk_panel_.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        content_panel_.add(save_to_disk_panel_, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        save_to_disk_panel_.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "Output for external fitter", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        path_selector_view_ = new PathSelectorView();
        save_to_disk_panel_.add(path_selector_view_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        save_to_disk_btn_ = new JButton();
        save_to_disk_btn_.setText("Save To Disk");
        save_to_disk_panel_.add(save_to_disk_btn_, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return content_panel_;
    }

}
