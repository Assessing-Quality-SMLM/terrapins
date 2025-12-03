package com.coxphysics.terrapins.views.assessment.results;

import com.coxphysics.terrapins.view_models.assessment.AssessmentVM;
import com.coxphysics.terrapins.view_models.assessment.results.FRCVM;
import com.coxphysics.terrapins.view_models.assessment.ReportVM;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ij.IJ;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

class FileDialogAction implements ActionListener
{

    private final ReportView view_;

    private FileDialogAction(ReportView view)
    {
        view_ = view;
    }

    public static FileDialogAction from(ReportView view)
    {
        return new FileDialogAction(view);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setCurrentDirectory(view_.data_path().toFile());
        int result = jfc.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION)
            return;
        File current_directory = jfc.getSelectedFile();
        view_.set_data_path(current_directory.toPath());
    }
}

class DataPathListner implements DocumentListener
{
    private final ReportView view_;

    private DataPathListner(ReportView view)
    {
        view_ = view;
    }

    public static DataPathListner from(ReportView view)
    {
        return new DataPathListner(view);
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        view_.update_data_path_from_view();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        view_.update_data_path_from_view();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        view_.update_data_path_from_view();
    }
}

class ShowDetailsAssessmentListener implements ActionListener
{
    private final ReportVM report_vm_;
    private final AssessmentView view_;
    private final Consumer<Boolean> action_;

    public ShowDetailsAssessmentListener(ReportVM report_vm, AssessmentView bias_view, Consumer<Boolean> action)
    {

        report_vm_ = report_vm;
        this.view_ = bias_view;
        this.action_ = action;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        action_.accept(view_.show_details());
    }
}

public class ReportView extends JFrame {
    private final ReportVM view_model_;
    private JPanel content_panel_;
    private JTextField data_path_;
    private AssessmentView localisation_assessment_;
    private AssessmentView blinking_assessment_;
    private JButton data_path_btn_;
    private AssessmentView bias_assessment_;
    private AssessmentView frc_resolution_assessment_;
    private AssessmentView sampling_assessment_;
    private JScrollPane scroll_panel_;
    private JPanel scrolling_panel_;
    private AssessmentView squirrel_assessment_;
    private AssessmentView drift_assessment_;
    private AssessmentView magnification_assessment_;
    private JTabbedPane tabbed_panel_;
    private JScrollPane results_scroll_pane_;
    private JCheckBox hawk_recon_;
    private JCheckBox recon_;
    private JCheckBox hawkman_;
    private JCheckBox average_of_frames_squirrel_;
    private JCheckBox widefield_squirrel_;
    private FRCView half_split_results_;
    private FRCView drift_split_results_;
    private FRCView zip_split_results_;

    private ReportView(ReportVM view_model) {
        view_model_ = view_model;
        data_path_btn_.addActionListener(FileDialogAction.from(this));
        data_path_.getDocument().addDocumentListener(DataPathListner.from(this));
    }

    public static ReportView from(ReportVM view_model) {
        ReportView view = new ReportView(view_model);
        view.setTitle("Results View");
        view.add(view.content_panel_);
        view.reset_data_path();
        view.drift_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.drift_assessment_, view_model::display_drift_details));
        view.magnification_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.magnification_assessment_, view_model::display_magnification_details));
        view.blinking_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.blinking_assessment_, view_model::display_blinking_details));
        view.sampling_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.sampling_assessment_, view_model::display_sampling_details));
//        view.localisation_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.localisation_assessment_, view_model::display_localisation_precision_details));
        view.frc_resolution_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.frc_resolution_assessment_, view_model::display_frc_resolution_details));
        view.bias_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.bias_assessment_, view_model::display_bias_details));
        view.squirrel_assessment_.add_details_listener(new ShowDetailsAssessmentListener(view_model, view.squirrel_assessment_, view_model::display_squirrel_details));
        return view;
    }

    public Path data_path() {
        return view_model_.data_path();
    }

    public void update_data_path_from_view() {
        String new_path_text = data_path_.getText();
        try {
            Path new_path = Paths.get(new_path_text);
            view_model_.set_data_path(new_path);
            update_views();
        } catch (Exception e) {
            String message = "Could not load path due to " + e;
            IJ.log(message);
        }
    }

    public void reset_data_path() {
        data_path_.setText(view_model_.data_path().toString());
        update_views();
    }

    private void update_views() {
        update_reports();
        update_results();
    }

    private void update_results() {
        update_half_split_results();
        update_drift_split_results();
        update_zip_split_results();
    }

    private void update_half_split_results() {
        FRCVM view_model = view_model_.half_split_results();
        half_split_results_.set_view_model(view_model);
    }

    private void update_drift_split_results() {
        FRCVM view_model = view_model_.drift_split_results();
        drift_split_results_.set_view_model(view_model);
    }

    private void update_zip_split_results() {
        FRCVM view_model = view_model_.zip_split_results();
        zip_split_results_.set_view_model(view_model);
    }

    private void update_reports() {
        update_drift_assessment();
        update_magnification_assessment();
        update_blinking_assessment();
        update_sampling_assessment();
        update_localisation_precision_assessment();
        update_frc_resolution_assessment();
        update_bias_assessment();
        update_squirrel_assessment();
    }

    private void update_drift_assessment() {
        AssessmentVM view_model = view_model_.drift_assessment();
        drift_assessment_.set_view_model(view_model);
    }

    private void update_magnification_assessment() {
        AssessmentVM view_model = view_model_.magnification_assessment();
        magnification_assessment_.set_view_model(view_model);
    }

    private void update_blinking_assessment() {
        AssessmentVM vieW_model = view_model_.blinking_assessment();
        blinking_assessment_.set_view_model(vieW_model);
    }

    private void update_sampling_assessment() {
        AssessmentVM view_model = view_model_.sampling_assessment();
        sampling_assessment_.set_view_model(view_model);
    }

    private void update_localisation_precision_assessment() {
        AssessmentVM view_model = view_model_.localisation_precision_assessment();
        localisation_assessment_.set_view_model(view_model);
    }

    private void update_frc_resolution_assessment() {
        AssessmentVM view_model = view_model_.frc_resolution_assessment();
        frc_resolution_assessment_.set_view_model(view_model);
    }

    private void update_bias_assessment() {
        AssessmentVM view_model = view_model_.bias_assessment();
        if (view_model == null)
            return;
        bias_assessment_.set_view_model(view_model);
    }

    private void update_squirrel_assessment() {
        AssessmentVM view_model = view_model_.squirrel_assessment();
        squirrel_assessment_.set_view_model(view_model);
    }

    public void set_data_path(Path value) {
        view_model_.set_data_path(value);
        reset_data_path();
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
        content_panel_.setLayout(new GridLayoutManager(4, 5, new Insets(10, 10, 2, 5), -1, -1));
        content_panel_.setAutoscrolls(false);
        data_path_ = new JTextField();
        content_panel_.add(data_path_, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Data Path");
        content_panel_.add(label1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        data_path_btn_ = new JButton();
        data_path_btn_.setLabel("Find");
        data_path_btn_.setText("Find");
        content_panel_.add(data_path_btn_, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabbed_panel_ = new JTabbedPane();
        content_panel_.add(tabbed_panel_, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbed_panel_.addTab("Reports", panel1);
        scroll_panel_ = new JScrollPane();
        panel1.add(scroll_panel_, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrolling_panel_ = new JPanel();
        scrolling_panel_.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        scroll_panel_.setViewportView(scrolling_panel_);
        localisation_assessment_ = new AssessmentView();
        scrolling_panel_.add(localisation_assessment_.$$$getRootComponent$$$(), new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frc_resolution_assessment_ = new AssessmentView();
        scrolling_panel_.add(frc_resolution_assessment_.$$$getRootComponent$$$(), new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bias_assessment_ = new AssessmentView();
        scrolling_panel_.add(bias_assessment_.$$$getRootComponent$$$(), new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        squirrel_assessment_ = new AssessmentView();
        scrolling_panel_.add(squirrel_assessment_.$$$getRootComponent$$$(), new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        drift_assessment_ = new AssessmentView();
        scrolling_panel_.add(drift_assessment_.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        blinking_assessment_ = new AssessmentView();
        scrolling_panel_.add(blinking_assessment_.$$$getRootComponent$$$(), new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sampling_assessment_ = new AssessmentView();
        scrolling_panel_.add(sampling_assessment_.$$$getRootComponent$$$(), new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        magnification_assessment_ = new AssessmentView();
        scrolling_panel_.add(magnification_assessment_.$$$getRootComponent$$$(), new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbed_panel_.addTab("Results", panel2);
        results_scroll_pane_ = new JScrollPane();
        panel2.add(results_scroll_pane_, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        results_scroll_pane_.setViewportView(panel3);
        hawk_recon_ = new JCheckBox();
        hawk_recon_.setLabel("HAWK Recon");
        hawk_recon_.setText("HAWK Recon");
        panel3.add(hawk_recon_, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        recon_ = new JCheckBox();
        recon_.setLabel("Recon");
        recon_.setText("Recon");
        panel3.add(recon_, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hawkman_ = new JCheckBox();
        hawkman_.setText("HAWKMAN");
        panel3.add(hawkman_, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        average_of_frames_squirrel_ = new JCheckBox();
        average_of_frames_squirrel_.setText("Average of Frames SQUIRREL");
        panel3.add(average_of_frames_squirrel_, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        widefield_squirrel_ = new JCheckBox();
        widefield_squirrel_.setText("True Widefield SQUIRREL");
        panel3.add(widefield_squirrel_, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        half_split_results_ = new FRCView();
        panel3.add(half_split_results_.$$$getRootComponent$$$(), new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        drift_split_results_ = new FRCView();
        panel3.add(drift_split_results_.$$$getRootComponent$$$(), new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        zip_split_results_ = new FRCView();
        panel3.add(zip_split_results_.$$$getRootComponent$$$(), new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return content_panel_;
    }

}
