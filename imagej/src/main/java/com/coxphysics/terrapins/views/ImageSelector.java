package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ImageSelector
{
    private final GenericDialog dialog_;

    private final int n_images_;

    private final ArrayList<StringChoice> image_choices_;



    private ImageSelector(GenericDialog dialog, int n_images, ArrayList<StringChoice> image_choices)
    {
        dialog_ = dialog;
        n_images_ = n_images;
        image_choices_ = image_choices;
    }

    public static ImageSelector add_to_dialog(GenericDialog dialog, ImageSelectorSetttings settings)
    {
        String[] image_titles = Utils.get_valid_image_choices(settings.n_images(), new Utils.ImageJImageTitleProvider());
        ArrayList<StringChoice> image_choices = new ArrayList<>();
        String[] labels = settings.image_names();
        for (int idx = 0; idx < settings.n_images(); idx++)
        {
            String image_title =  idx < image_titles.length ? image_titles[idx] : "";
            String label = labels[idx];
            StringChoice choice = Utils.add_string_choice(dialog, label, image_titles, image_title);
            choice.set_visible(settings.visible());
            image_choices.add(choice);
        }
        return new ImageSelector(dialog, settings.n_images(), image_choices);
    }

    public String[] extract_image_names_recorded(GenericDialog dialog)
    {
        String[] image_names = new String[n_images_];
        for (int idx = 0; idx < n_images_; idx++)
        {
            image_names[idx] = Utils.extract_string_choice(dialog);
        }
        return image_names;
    }

    public String[] read_image_names()
    {
        String[] image_names = new String[n_images_];
        for (int idx = 0; idx < n_images_; idx++)
        {
            image_names[idx] = image_choices_.get(idx).choice();
        }
        return image_names;
    }

    public void reset_image_names()
    {
        String[] titles = Utils.get_valid_image_choices_from_image_j(n_images_);
        for (StringChoice choice : image_choices_)
        {
            choice.reset_choices(titles);
        }
    }

    public ActionListener reset_images_listener()
    {
        return new ResetImageChoicesListener(this);
    }

    public void set_visibility(boolean value)
    {
        for (StringChoice choice : image_choices_)
        {
            choice.set_visible(value);
        }
    }

    public void set_enabled(boolean value)
    {
        for (StringChoice choice : image_choices_)
        {
            choice.set_enabled(value);
        }
    }

    private static class ResetImageChoicesListener implements ActionListener
    {
        private final ImageSelector image_selector_;

        public ResetImageChoicesListener(ImageSelector ui)
        {
            image_selector_ = ui;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            image_selector_.reset_image_names();
            image_selector_.dialog_.pack();
        }
    }
}
