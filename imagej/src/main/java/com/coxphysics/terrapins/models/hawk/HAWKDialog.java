package com.coxphysics.terrapins.models.hawk;

import com.coxphysics.terrapins.views.hawk.HawkUI;
import ij.gui.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;

///Dialog box for starting HAWK
///The dialog highlights bad things in red.
///@ingroup gPlugin
//Not pretty. Should I use dialogItemChanged rather than textValueChanged???
public class HAWKDialog extends GenericDialog
{
    private final Color dialog_background_;

    private final int n_frames_;


    private final HawkUI ui_;

    HAWKDialog(int n_frames)
    {
        super("HAWK");

        n_frames_ = n_frames;

        ui_ = HawkUI.add_to_dialog(this, Settings.default_());

        dialog_background_ = getBackground(); //Dialog background color

        getTextArea1().setEditable(false);
        getTextArea1().removeTextListener(this); //To prevent event thrashing when we write messages

        //Process initial warnings
        textValueChanged(null);
    }

    private String validate_n_levels()
    {
        int n_levels = ui_.n_levels();
        if(n_levels > 5)
        {
            return "Warning: Diminishing returns and slow runs about 5 levels";
        }
        if(n_levels < 1)
        {
            return "Warning: At least 1 level required";
        }
        return "";
    }

    private String validate_config()
    {
        Config config = ui_.read_config();
        String errors = config.get_validation_errors(n_frames_);
        if (errors == null)
        {
            return "";
        }
        else
        {
            return errors;
        }
    }

    private void set_error_message(String message)
    {
        getTextArea1().setText(message);
        getTextArea1().setBackground(Color.RED);
    }
    private void clear_errors()
    {
        getTextArea1().setText("");
        getTextArea1().setBackground(dialog_background_);
    }

    public void itemStateChanged(ItemEvent e)
    {
        change();
    }

    public void textValueChanged(TextEvent e)
    {
        change();
    }

    void change()
    {
        clear_errors();
        String error =get_error_string();
        if (!error.isEmpty())
            set_error_message(error);
        repaint();

    }

    private String get_error_string()
    {
        String n_levels_error = validate_n_levels();
        String config_errors = validate_config();
        if (n_levels_error.isEmpty())
            return config_errors;
        return String.join("\n", n_levels_error, config_errors);
    }
}