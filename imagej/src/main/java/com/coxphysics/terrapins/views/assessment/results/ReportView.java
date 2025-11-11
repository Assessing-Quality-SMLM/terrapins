package com.coxphysics.terrapins.views.assessment.results;

import com.coxphysics.terrapins.view_models.assessment.ReportVM;

import javax.swing.*;

public class ReportView extends JFrame
{
    private final ReportVM view_model_;
    private JPanel content_panel_;
    private JTextField data_path_;
    private AssessmentView localisation_assessment_;
    private AssessmentView blinking_assessment_;
    private JButton data_path_btn_;

    private ReportView(ReportVM view_model)
    {
        view_model_ = view_model;
    }

    public static ReportView from(ReportVM view_model)
    {
        ReportView view = new ReportView(view_model);
        view.setTitle("Something");
        view.add(view.content_panel_);
        view.reset_data_path();
        view.display();
        return view;
    }

    private void display()
    {
//        content_panel_.setVisible(true);
//        data_path_.setVisible(true);
    }

    public void reset_data_path()
    {
        data_path_.setText(view_model_.data_path().toString());
//        pack();
    }
}
