package com.coxphysics.terrapins.models.renderer;

import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.process.FakeRunner;
import ij.ImagePlus;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RendererTests
{
     @Test
    public void is_valid()
    {
        Renderer r = Renderer.custom(null);
        assertEquals(r.is_valid(), false);
    }

    @Test
    public void output_directory()
    {
        Renderer r = Renderer.custom(Paths.get("here", "an.exe"));
        assertEquals(r.default_output_directory(), Paths.get("here", "renderer_data"));
    }

    @Test
    public void default_output_image_location()
    {
        Renderer r = Renderer.custom(Paths.get("here", "an.exe"));
        assertEquals(r.output_image_path(RenderSettings.default_()), Paths.get("here", "renderer_data", "image.tiff").toString());
    }

    @Test
    public void output_image_location()
    {
        Renderer r = Renderer.custom(Paths.get("here", "an.exe"));
        RenderSettings settings = RenderSettings.default_();
        settings.set_image_path("some/where/image.tiff");
        assertEquals(r.output_image_path(settings), "some/where/image.tiff");
    }

    @Test
    public void on_process_error_code_get_null()
    {
        Renderer r = Renderer.custom(Paths.get("here", "an.exe"));
        RenderSettings settings = RenderSettings.default_();
        ImagePlus image =  r.render_localisations_with(FakeRunner.with_exit_code(1), "a/file.txt", settings);
        assertNull(image);
    }

    @Test
    public void global_reference_frame_can_be_specified()
    {
        Path exe_path = Paths.get("here", "an.exe");
        String output_path = Paths.get("here", "renderer_data", "image.tiff").toString();
        Renderer r = Renderer.custom(exe_path);
        RenderSettings settings = RenderSettings.default_();
        settings.set_n_threads(1);
        settings.set_global_reference_frame("1,2,3,4");
        List<String> commands = r.get_commands("file.txt", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "-i", "file.txt", "-o", output_path, "-c", "100.0", "-m", "10", "-g", "1,2,3,4", "-s", "3.0", "-n", "1", "--f32",  "--parse-method", "ts");
        assertEquals(commands, expected);
    }

    @Test
    public void csv_can_be_specified()
    {
        Path exe_path = Paths.get("here", "an.exe");
        String output_path = Paths.get("here","renderer_data", "image.tiff").toString();
        Renderer r = Renderer.custom(exe_path);
        RenderSettings settings = RenderSettings.default_();
        settings.set_n_threads(1);
        ParseMethod parse_method = settings.parse_method_settings();
        parse_method.set_parse_method_csv();
        parse_method.set_n_headers(2);
        parse_method.set_x_pos(1);
        parse_method.set_y_pos(2);
        parse_method.set_uncertainty_sigma_pos(3);
        List<String> commands = r.get_commands("file.txt", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "-i", "file.txt", "-o", output_path, "-c", "100.0", "-m", "10", "-g", "0,0,256,256", "-s", "3.0", "-n", "1", "--f32",  "--parse-method", "csv=2;,;1;2;3;-1;-1");
        assertEquals(commands, expected);
    }
}