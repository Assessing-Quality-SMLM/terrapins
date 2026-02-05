package com.coxphysics.terrapins.views.TERRAPINS;

import com.coxphysics.terrapins.view_models.TERRAPINS.PathSelectorVM;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

class FindListener implements ActionListener
{
    private final PathSelectorView view_;

    private FindListener(PathSelectorView view)
    {
        view_ = view;
    }

    public static FindListener from(PathSelectorView view)
    {
        return new FindListener(view);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        view_.find_path();
    }
}

public class PathSelectorView {
    private JPanel root_;
    private JTextField filename_txt_field_;
    private JButton find_btn_;
    private JLabel label_;

    private PathSelectorVM view_model_ = PathSelectorVM.default_();

    public PathSelectorView()
    {
        find_btn_.addActionListener(FindListener.from(this));
    }

    private PathSelectorView(PathSelectorVM view_model)
    {
        super();
        view_model_ = view_model;

    }

    public static PathSelectorView from(PathSelectorVM view_model)
    {
        PathSelectorView view = new PathSelectorView(view_model);
        return view;
    }

    public void find_path()
    {
        Path new_path = view_model_.find_path();
        filename_txt_field_.setText(new_path.toString());
    }
}
