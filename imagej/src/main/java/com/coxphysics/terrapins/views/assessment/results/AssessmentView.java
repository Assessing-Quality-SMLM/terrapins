package com.coxphysics.terrapins.views.assessment.results;

import com.coxphysics.terrapins.view_models.assessment.AssessmentVM;

import javax.swing.*;
import java.awt.*;

public class AssessmentView
{
    private JPanel content_panel_;
    private JTextArea details_;
    private JLabel name_;
    private JCheckBox show_results_;
    private JLabel score_;
    private JLabel passed_;
    private JPanel passed_panel_;

    private AssessmentVM view_model_;

    public void set_view_model(AssessmentVM view_model)
    {
        view_model_ = view_model;
        update_data();
    }

    private void update_data()
    {
        name_.setText(view_model_.name());
        score_.setText(view_model_.score_text());
        passed_.setText(view_model_.passed_text());
        passed_panel_.setBackground(view_model_.background_colour());
        details_.setText(view_model_.message());
    }
}
