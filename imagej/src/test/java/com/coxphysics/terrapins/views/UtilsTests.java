package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTests
{
    @Test
    public void add_null_text_areas()
    {
        GenericDialog dialog = new GenericDialog("something");
        TextAreas text_areas = Utils.add_text_areas(dialog, null, null, 1, 2);
        assertNotNull(text_areas.area_1());
        assertEquals(text_areas.area_1_text(), "");
        assertNull(text_areas.area_2());
        assertEquals(text_areas.area_2_text(), "");
    }

    @Test
    public void add_text_area_1()
    {
        GenericDialog dialog = new GenericDialog("something");
        TextAreas text_areas = Utils.add_text_areas(dialog, "a", null, 1, 2);
        assertNotNull(text_areas.area_1());
        assertEquals(text_areas.area_1_text(), "a");
        assertNull(text_areas.area_2());
        assertEquals(text_areas.area_2_text(), "");
    }

    @Test
    public void add_text_area_2()
    {
        GenericDialog dialog = new GenericDialog("something");
        TextAreas text_areas = Utils.add_text_areas(dialog, null, "a", 1, 2);
        assertNotNull(text_areas.area_1());
        assertEquals(text_areas.area_1_text(), "");
        assertNotNull(text_areas.area_2());
        assertEquals(text_areas.area_2_text(), "a");
    }

    @Test
    public void add_text_areas()
    {
        GenericDialog dialog = new GenericDialog("something");
        TextAreas text_areas = Utils.add_text_areas(dialog, "a", "b", 1, 2);
        assertNotNull(text_areas.area_1());
        assertEquals(text_areas.area_1_text(), "a");
        assertNotNull(text_areas.area_2());
        assertEquals(text_areas.area_2_text(), "b");
    }

    @Test
    public void add_file_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        FileField field = Utils.add_file_field(dialog, "a", "thing");
        assertEquals(field.filepath(), "thing");
    }

    @Test
    public void extract_file_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        FileField field = Utils.add_file_field(dialog, "a", "thing");
        assertEquals(field.filepath(), "thing");
        assertEquals(Utils.extract_file_field(dialog), "thing");
        assertEquals(field.filepath(), "thing");
    }

    @Test
    public void add_checkbox()
    {
        GenericDialog dialog = new GenericDialog("something");
        Checkbox checkbox  = Utils.add_checkbox(dialog, "a", true);
        assertEquals(checkbox.label(), "a");
        assertTrue(checkbox.is_checked());
    }

    @Test
    public void extract_checkbox()
    {
        GenericDialog dialog = new GenericDialog("something");
        Checkbox checkbox  = Utils.add_checkbox(dialog, "a", true);
        assertTrue(checkbox.is_checked());
        assertTrue(Utils.extract_checkbox_value(dialog));
    }

    @Test
    public void add_string_choice()
    {
        GenericDialog dialog = new GenericDialog("something");
        String[] choices = new String[]{"b", "c"};
        StringChoice choice  = Utils.add_string_choice(dialog, "a", choices, "c");
        assertEquals(choice.choice(), "c");
    }

    @Test
    public void extract_string_choice()
    {
        GenericDialog dialog = new GenericDialog("something");
        String[] choices = new String[]{"b", "c"};
        StringChoice choice  = Utils.add_string_choice(dialog, "a", choices, "c");
        assertEquals(choice.choice(), "c");
        assertEquals(Utils.extract_string_choice(dialog), "c");
    }

     @Test
    public void add_string_choice_non_option()
    {
        GenericDialog dialog = new GenericDialog("something");
        String[] choices = new String[]{"b", "c"};
        StringChoice choice  = Utils.add_string_choice(dialog, "a", choices, "blah");
        assertEquals(choice.choice(), "b");
    }

    @Test
    public void add_enum_choice()
    {
        GenericDialog dialog = new GenericDialog("something");
        EnumChoice choice = Utils.add_enum_choice(dialog, "a", TestEnum.ONE);
        assertEquals(choice.text(), "ONE");
    }

    @Test
    public void extract_enum_choice()
    {
        GenericDialog dialog = new GenericDialog("something");
        EnumChoice choice = Utils.add_enum_choice(dialog, "a", TestEnum.ONE);
        assertEquals(choice.text(), "ONE");
        assertEquals(Utils.extract_enum_choice(dialog, TestEnum.class), TestEnum.ONE);
    }

    @Test
    public void add_numeric_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        NumericField field = Utils.add_numeric_field(dialog, "a", 10, 0);
        assertEquals(field.text(), "10");
    }

    @Test
    public void extract_numeric_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        NumericField field = Utils.add_numeric_field(dialog, "a", 10, 0);
        assertEquals(field.text(), "10");
        assertEquals(Utils.extract_numeric_field_as_int(dialog), 10);
    }

    private enum TestEnum
    {
        ONE, TWO
    }


    @Test
    public void add_string_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        StringField field = Utils.add_string_field(dialog, "a field", "value");
        assertEquals(field.text(), "value");
    }

    @Test
    public void extract_string_field()
    {
        GenericDialog dialog = new GenericDialog("something");
        StringField field = Utils.add_string_field(dialog, "a field", "value");
        assertEquals(field.text(), "value");
        assertEquals(Utils.extract_string_field(dialog), "value");
    }

    @Test
    public void add_radio_button()
    {
        GenericDialog dialog = new GenericDialog("something");
        String[] choices = new String[]{"a", "b"};
        RadioButtons field = Utils.add_radio_buttons(dialog, "buttons", choices, 2, 1, "a");
        assertEquals(field.get_choice(), "a");
    }

    @Test
    public void extract_radio_button()
    {
        GenericDialog dialog = new GenericDialog("something");
        String[] choices = new String[]{"a", "b"};
        RadioButtons field = Utils.add_radio_buttons(dialog, "buttons", choices, 2, 1, "a");
        assertEquals(field.get_choice(), "a");
        assertEquals(Utils.extract_radio_buttons(dialog), "a");
    }

    @Test
    public void minimum_number_of_image_titles_when_no_titles_given()
    {
        Utils.ImageTitleProvider provider = new FakeImageTitleProvider(new String[0]);
        String[] titles = Utils.get_valid_image_choices(2, provider);
        String[] expected = {"", ""};
        assertArrayEquals(titles, expected);
    }

    @Test
    public void use_titles_when_enough_provided()
    {
        Utils.ImageTitleProvider provider = new FakeImageTitleProvider(new String[]{"A", "B"});
        String[] titles = Utils.get_valid_image_choices(2, provider);
        String[] expected = {"A", "B"};
        assertArrayEquals(titles, expected);
    }

    @Test
    public void can_handle_gt_minimum_existing_image()
    {
        Utils.ImageTitleProvider provider = new FakeImageTitleProvider(new String[]{"A", "B", "C"});
        String[] titles = Utils.get_valid_image_choices(2, provider);
        String[] expected = {"A", "B", "C"};
        assertArrayEquals(titles, expected);
    }

    private static class FakeImageTitleProvider implements Utils.ImageTitleProvider
    {

        private final String[] titles_;

        public FakeImageTitleProvider(String[] titles)
        {
            titles_ = titles;
        }

        @Override
        public String[] get_image_titles()
        {
            return titles_;
        }
    }
}
