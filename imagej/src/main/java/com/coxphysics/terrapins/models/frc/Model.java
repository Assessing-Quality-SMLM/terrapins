package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.localisations.Splitter;
import com.coxphysics.terrapins.models.renderer.RenderSettings;
import com.coxphysics.terrapins.models.renderer.Renderer;
import com.coxphysics.terrapins.models.utils.FsUtils;
import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.ImagePlus;
import ij.WindowManager;

import java.util.List;

public class Model
{
    public static FRCResult fire_from(FRCDialogSettings settings)
    {
        Images images = get_images(settings);
        if (!images.is_valid())
            return null;
        images.show();
        FRC calculator = FRC.default_();
        if (!calculator.is_valid())
            return null;
        if (!FsUtils.prepare_directory(calculator.default_output_directory()))
            return null;
        images.write_to_disk(calculator.default_output_directory(), settings);
        FRCResult result = calculator.calculate_fire_number(settings);
        if (result == null)
            return null;
        Plotter.plot(result);
        return result;
    }

    public static Images get_images(FRCDialogSettings settings)
    {
        if (settings.use_existing_images())
        {
            return existing_images(settings);
        }
        if (settings.split_specified())
        {
            return render_localisation_files(settings);
        }
        else
        {
            return split_and_render(settings);
        }
    }
    private static Images existing_images(FRCDialogSettings settings)
    {
        ImagePlus image_1 = WindowManager.getImage(settings.image_1());
        ImagePlus image_2 = WindowManager.getImage(settings.image_2());
        return Images.from(image_1, image_2);
    }

    private static Images render_localisation_files(FRCDialogSettings settings)
    {
        RenderSettings render_settings = settings.render_settings();
        ImagePlus image_1 = render_localisations(settings.localisation_path(), render_settings);
        ImagePlus image_2 = render_localisations(settings.extra_localisation_path(), render_settings);
        return Images.from(image_1, image_2);
    }

    private static Images split_and_render(FRCDialogSettings settings)
    {
        String localisation_file = settings.localisation_path();
        if (!StringUtils.path_set(localisation_file))
            return Images.invalid();
        List<String> localisation_files =  Splitter.default_().split(localisation_file, settings.split_settings());
        if (localisation_files == null || localisation_files.size() < 2)
            return Images.invalid();
        RenderSettings render_settings = settings.render_settings();
        ImagePlus image_1 = render_localisations(localisation_files.get(0), render_settings);
        ImagePlus image_2 = render_localisations(localisation_files.get(1), render_settings);
        return Images.from(image_1, image_2);
    }

    private static ImagePlus render_localisations(String localisation_path, RenderSettings render_settings)
    {
        return Renderer.default_().render_localisations(localisation_path, render_settings);
    }
}
