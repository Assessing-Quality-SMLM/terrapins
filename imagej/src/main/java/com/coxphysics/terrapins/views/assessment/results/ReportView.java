package com.coxphysics.terrapins.views.assessment.results;

import com.coxphysics.terrapins.view_models.assessment.AssessmentVM;
import com.coxphysics.terrapins.view_models.assessment.ReportVM;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        data_path_.getDocument().addDocumentListener(DataPathListner.from(this));
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

    public void update_data_path_from_view()
    {
        String new_path = data_path_.getText();
        view_model_.set_data_path(Paths.get(new_path));
        update_views();
    }

    public void reset_data_path()
    {
        data_path_.setText(view_model_.data_path().toString());
        update_views();
    }

    private void update_views()
    {
        update_blinking_assessment();
    }

    private void update_blinking_assessment()
    {
        AssessmentVM blinking_view_model = view_model_.blinking_assessment();
        if (blinking_view_model == null)
            return;
        blinking_assessment_.set_view_model(blinking_view_model);
    }

    public void set_data_path(Path value)
    {
        view_model_.set_data_path(value);
        reset_data_path();
    }
}
