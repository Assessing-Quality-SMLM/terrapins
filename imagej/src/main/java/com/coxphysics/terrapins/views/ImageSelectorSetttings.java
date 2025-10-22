package com.coxphysics.terrapins.views;

public class ImageSelectorSetttings
{
    private int n_images_ = 2;

    private String image_name_prefix_ = "Image";

    private String[] image_names_ = null;

    private boolean visible_ = true;

    private ImageSelectorSetttings(int n_images, String[] image_names, String image_name_prefix, boolean visible)
    {
        n_images_ = n_images;
        image_name_prefix_ = image_name_prefix;
        image_names_ = image_names;
        visible_ = visible;
    }

    public static ImageSelectorSetttings default_()
    {
        return new ImageSelectorSetttings(2, null, "Image", true);
    }

    public int n_images()
    {
        return n_images_;
    }

    public void set_n_images(int value)
    {
        n_images_ = value;
    }

    public String[] image_names()
    {
        if (image_names_ == null)
            return generate_image_names();
        return image_names_;
    }

    public boolean set_image_names(String[] image_names)
    {
        if (image_names.length < n_images_)
            return false;
        image_names_ = image_names;
        return true;
    }

    private String[] generate_image_names()
    {
        String[] image_names = new String[n_images_];
        for (int idx = 0; idx < n_images_; idx++)
        {
            image_names[idx] = image_name_prefix_ + " " + (idx + 1);
        }
        return image_names;
    }

    public boolean visible()
    {
        return visible_;
    }

    public void set_visible(boolean value)
    {
        visible_ = value;
    }
}
