package com.coxphysics.terrapins.views.TERRAPINS;

import com.coxphysics.terrapins.view_models.TERRAPINS.LocalisationFileVM;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

class ActionableListener implements DocumentListener
{
    private LocalisationFileView view_;
    private final Consumer<LocalisationFileView> action_;

    private ActionableListener(LocalisationFileView view, Consumer<LocalisationFileView> action)
    {
        view_ = view;
        action_ = action;
    }

    public static ActionableListener from(LocalisationFileView view, Consumer<LocalisationFileView> action)
    {
        return new ActionableListener(view, action);
    }
    @Override
    public void insertUpdate(DocumentEvent e)
    {
        action_.accept(view_);
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        action_.accept(view_);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        action_.accept(view_);
    }
}

class ThunderstormListener implements ActionListener
{
    private LocalisationFileView view_;

    private ThunderstormListener(LocalisationFileView view)
    {
        view_ = view;
    }

    public static ThunderstormListener from(LocalisationFileView view)
    {
        return new ThunderstormListener(view);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        view_.thunderstorm_updated();
    }
}

public class LocalisationFileView {
    private JPanel root_;
    private JCheckBox use_thunderstorm_;
    private PathSelectorView path_selector_ctrl_;
    private JTextField delimiter_field_;
    private JLabel delimiter_lbl_;
    private JTextField n_headers_field_;
    private JLabel n_headers_lbl_;
    private JTextField x_pos_field_;
    private JLabel x_pos_lbl_;
    private JLabel y_pos_lbl_;
    private JTextField y_pos_field_;
    private JLabel uncertainty_sigma_lbl_;
    private JTextField uncertainty_sigma_field_;
    private JLabel frame_number_pos_lbl_;
    private JTextField frame_number_pos_field_;

    private LocalisationFileVM view_model_ = LocalisationFileVM.default_();

    public LocalisationFileView()
    {
        path_selector_ctrl_.set_view_model(view_model_.path_selector_vm());
        delimiter_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_delimeter));
        n_headers_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_n_header_lines));
        x_pos_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_x_pos));
        y_pos_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_y_pos));
        uncertainty_sigma_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_uncertainty_sigma_pos));
        frame_number_pos_field_.getDocument().addDocumentListener(ActionableListener.from(this, LocalisationFileView::update_frame_number_pos));
        use_thunderstorm_.addActionListener(ThunderstormListener.from(this));
//        path_selector_ctrl_.add_listener_to_filename_change(ActionableListener.from(this, LocalisationFileView::update_filepath));
    }

    public void set_view_model(LocalisationFileVM view_model)
    {
        view_model_ = view_model;
        draw();
    }

    public void finalise_settings()
    {
        update_model_filepath();
    }

    public void thunderstorm_updated()
    {
        boolean value = use_thunderstorm_.isSelected();
        view_model_.set_use_thunderstorm(value);
        csv_visible(!value);
    }


    private void csv_visible(boolean value)
    {
        delimiter_lbl_.setVisible(value);
        delimiter_field_.setVisible(value);

        n_headers_lbl_.setVisible(value);
        n_headers_field_.setVisible(value);

        x_pos_lbl_.setVisible(value);
        x_pos_field_.setVisible(value);

        y_pos_lbl_.setVisible(value);
        y_pos_field_.setVisible(value);

        uncertainty_sigma_lbl_.setVisible(value);
        uncertainty_sigma_field_.setVisible(value);

        frame_number_pos_lbl_.setVisible(value);
        frame_number_pos_field_.setVisible(value);
    }

    public void draw()
    {
        path_selector_ctrl_.draw();
        delimiter_field_.setText(Character.toString(view_model_.delimeter()));
        n_headers_field_.setText(Integer.toString(view_model_.n_header_lines()));
        x_pos_field_.setText(Integer.toString(view_model_.x_pos()));
        y_pos_field_.setText(Integer.toString(view_model_.y_pos()));
        uncertainty_sigma_field_.setText(Integer.toString(view_model_.uncertainty_sigma_pos()));
        frame_number_pos_field_.setText(Integer.toString(view_model_.frame_number_pos()));
        boolean use_thunderstorm = view_model_.use_thunderstorm();
        use_thunderstorm_.setSelected(use_thunderstorm);
        csv_visible(!use_thunderstorm);
    }

    private void update_delimeter()
    {
        view_model_.set_delimeter(delimiter_field_.getText());
    }

    private void update_n_header_lines()
    {
        view_model_.set_n_header_lines(n_headers_field_.getText());
    }

    private void update_x_pos()
    {
        view_model_.set_x_pos(x_pos_field_.getText());
    }

    private void update_y_pos()
    {
        view_model_.set_y_pos(y_pos_field_.getText());
    }

    private void update_uncertainty_sigma_pos()
    {
        view_model_.set_uncertainty_sigma_pos(uncertainty_sigma_field_.getText());
    }

    private void update_frame_number_pos()
    {
        view_model_.set_frame_number_pos(frame_number_pos_field_.getText());
    }

    private void update_model_filepath()
    {
        view_model_.update_model_path();
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
        root_ = new JPanel();
        root_.setLayout(new GridLayoutManager(9, 3, new Insets(5, 5, 5, 5), -1, -1));
        path_selector_ctrl_ = new PathSelectorView();
        root_.add(path_selector_ctrl_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        root_.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        root_.add(spacer2, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        use_thunderstorm_ = new JCheckBox();
        use_thunderstorm_.setText("Thunderstorm");
        root_.add(use_thunderstorm_, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        delimiter_field_ = new JTextField();
        root_.add(delimiter_field_, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        delimiter_lbl_ = new JLabel();
        delimiter_lbl_.setText("Delimeter");
        root_.add(delimiter_lbl_, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        n_headers_lbl_ = new JLabel();
        n_headers_lbl_.setText("Number of Header Lines");
        root_.add(n_headers_lbl_, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        n_headers_field_ = new JTextField();
        root_.add(n_headers_field_, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        x_pos_lbl_ = new JLabel();
        x_pos_lbl_.setText("X pos");
        root_.add(x_pos_lbl_, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        x_pos_field_ = new JTextField();
        root_.add(x_pos_field_, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        y_pos_lbl_ = new JLabel();
        y_pos_lbl_.setText("Y pos");
        root_.add(y_pos_lbl_, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        y_pos_field_ = new JTextField();
        root_.add(y_pos_field_, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        uncertainty_sigma_lbl_ = new JLabel();
        uncertainty_sigma_lbl_.setText("Uncertainty Sigma");
        root_.add(uncertainty_sigma_lbl_, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uncertainty_sigma_field_ = new JTextField();
        root_.add(uncertainty_sigma_field_, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        frame_number_pos_lbl_ = new JLabel();
        frame_number_pos_lbl_.setText("Fram Number");
        root_.add(frame_number_pos_lbl_, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frame_number_pos_field_ = new JTextField();
        root_.add(frame_number_pos_field_, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root_;
    }
}
