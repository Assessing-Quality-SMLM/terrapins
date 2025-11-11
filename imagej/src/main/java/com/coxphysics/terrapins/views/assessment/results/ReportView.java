package com.coxphysics.terrapins.views.assessment.results;

import com.coxphysics.terrapins.view_models.assessment.ReportVM;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;

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
        data_path_btn_.addActionListener(FileDialogAction.from(this));
    }

    public static ReportView from(ReportVM view_model)
    {
        ReportView view = new ReportView(view_model);
        view.setTitle("Something");
        view.add(view.content_panel_);
        view.reset_data_path();
        return view;
    }

    public Path data_path()
    {
        return view_model_.data_path();
    }

    public void reset_data_path()
    {
        data_path_.setText(view_model_.data_path().toString());
//        pack();
    }

    public void set_data_path(Path value)
    {
        view_model_.set_data_path(value);
        reset_data_path();
    }
}
