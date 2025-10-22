package com.coxphysics.terrapins.views;

import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.WindowManager;
import ij.gui.GenericDialog;

import java.awt.event.ActionListener;
import java.util.Arrays;

public class Utils
{
    public static DirectoryField add_directory_field(GenericDialog dialog, String name, String default_text)
    {
        dialog.addDirectoryField(name, default_text);
        return DirectoryField.from(dialog);
    }
    public static FileField add_file_field(GenericDialog dialog, String name, String default_text)
    {
        dialog.addFileField(name, default_text);
        return FileField.from(dialog);
    }

    public static String extract_file_field(GenericDialog dialog)
    {
        return dialog.getNextString();
    }

    public static <E extends Enum<E>> EnumChoice add_enum_choice(GenericDialog dialog, String name, E default_text)
    {
        dialog.addEnumChoice(name, default_text);
        return EnumChoice.from(dialog);
    }


    public static <E extends Enum<E>> E extract_enum_choice(GenericDialog dialog, Class<E> e)
    {
        return dialog.getNextEnumChoice(e);
    }

    public static Checkbox add_checkbox(GenericDialog dialog, String name, boolean value)
    {
        dialog.addCheckbox(name, value);
        return Checkbox.from(dialog);
    }

    public static boolean extract_checkbox_value(GenericDialog dialog)
    {
        return dialog.getNextBoolean();
    }

    public static StringChoice add_string_choice(GenericDialog dialog, String name, String[] choices, String value)
    {
        dialog.addChoice(name, choices, value);
        return StringChoice.from(dialog);
    }

    public static String extract_string_choice(GenericDialog dialog)
    {
        return dialog.getNextChoice();
    }

    public static StringField add_string_field(GenericDialog dialog, String name, String text)
    {
        dialog.addStringField(name, text);
        return StringField.from(dialog);
    }

    public static String extract_string_field(GenericDialog dialog)
    {
        return dialog.getNextString();
    }

    public static NumericField add_numeric_field(GenericDialog dialog, String name, double value, int digits)
    {
        dialog.addNumericField(name, value, digits);
        return NumericField.from(dialog);
    }

    public static int extract_numeric_field_as_int(GenericDialog dialog)
    {
        return (int)extract_numeric_field(dialog);
    }

    public static double extract_numeric_field(GenericDialog dialog)
    {
        return dialog.getNextNumber();
    }

    public static RadioButtons add_radio_buttons(GenericDialog dialog, String name, String[] choices, int rows, int cols, String choice)
    {
        dialog.addRadioButtonGroup(name, choices, rows, cols, choice);
        return RadioButtons.from(dialog);
    }

    public static String extract_radio_buttons(GenericDialog dialog)
    {
        return dialog.getNextRadioButton();
    }

    public static Button add_button(GenericDialog dialog, String name, ActionListener listener)
    {
        dialog.addButton(name, listener);
        return Button.from(dialog);
    }

    public static Message add_message(GenericDialog dialog, String message)
    {
        dialog.addMessage(message);
        return Message.from(dialog);
    }

    public interface ImageTitleProvider
    {
        String[] get_image_titles();
    }

    public static class ImageJImageTitleProvider implements ImageTitleProvider
    {
        @Override
        public String[] get_image_titles()
        {
            return WindowManager.getImageTitles();
        }
    }

    public static String[] get_valid_image_choices_from_image_j(int min_number_of_images)
    {
        return get_valid_image_choices(min_number_of_images, new ImageJImageTitleProvider());
    }

    public static String[] get_valid_image_choices(int min_number_of_images, ImageTitleProvider provider)
    {
        String[] existing_titles = provider.get_image_titles();
        if (existing_titles.length >= min_number_of_images)
        {
            return existing_titles;
        }

        String[] image_titles = new String[min_number_of_images];
        Arrays.fill(image_titles, StringUtils.EMPTY_STRING);

        System.arraycopy(existing_titles, 0, image_titles, 0, existing_titles.length);

        return image_titles;
    }
}

