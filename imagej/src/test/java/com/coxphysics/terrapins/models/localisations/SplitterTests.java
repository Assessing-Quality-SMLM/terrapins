package com.coxphysics.terrapins.models.localisations;

import com.coxphysics.terrapins.models.fs.FakeFileSystem;
import com.coxphysics.terrapins.models.process.FakeRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SplitterTests
{

    @Test
    void null_on_bad_output()
    {
        SplitSettings settings = SplitSettings.default_();
        List<String> commands = Splitter.custom(Paths.get("/some/where/fake_split")).split_localisations_with(FakeRunner.with_exit_code(1), FakeFileSystem.True(), "something", settings);
        assertNull(commands);
    }

    @Test
    void null_if_cant_manipulate_filesystem()
    {
        SplitSettings settings = SplitSettings.default_();
        List<String> commands = Splitter.custom(Paths.get("/some/where/fake_split")).split_localisations_with(FakeRunner.with_exit_code(0), FakeFileSystem.False(), "something", settings);
        assertNull(commands);
    }

    @Test
    void set_a_file()
    {
        SplitSettings settings = SplitSettings.default_();
        settings.set_output_1("_file_a");
        Path exe_path = Paths.get("/some/where/fake_split");
        Path default_b = Paths.get("/some/where/splitter_data/localisations_split_b");
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", "_file_a",  "-b", default_b.toString(), "-m", "half", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void set_b_file()
    {
        SplitSettings settings = SplitSettings.default_();
        settings.set_output_2("_file_b");
        Path exe_path = Paths.get("/some/where/fake_split");
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", "_file_b", "-m", "half", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void set_half_split()
    {
        SplitSettings settings = SplitSettings.default_();
        settings.set_half_split();
        Path exe_path = Paths.get("/some/where/fake_split");
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        String default_b = Paths.get("/some/where/splitter_data/localisations_split_b").toString();
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", default_b, "-m", "half", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void set_zip_split()
    {
        SplitSettings settings = SplitSettings.default_();
        settings.set_zip_split();
        Path exe_path = Paths.get("/some/where/fake_split");
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        String default_b = Paths.get("/some/where/splitter_data/localisations_split_b").toString();
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", default_b, "-m", "zip", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void set_rand_split()
    {
        SplitSettings settings = SplitSettings.default_();
        settings.set_random_split();
        Path exe_path = Paths.get("/some/where/fake_split");
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        String default_b = Paths.get("/some/where/splitter_data/localisations_split_b").toString();
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", default_b, "-m", "rand", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void parse_method_passed_to_commands()
    {
        SplitSettings settings = SplitSettings.default_();
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_thunderstorm();
        settings.set_parse_method(parse_method);
        Path exe_path = Paths.get("/some/where/fake_split");
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        String default_b = Paths.get("/some/where/splitter_data/localisations_split_b").toString();
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", default_b, "-m", "half", "--parse-method", "ts");
        assertEquals(expected, commands);
    }

    @Test
    void can_use_csv_parsing()
    {
        SplitSettings settings = SplitSettings.default_();
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_delimiter(',');
        parse_method.set_n_headers(10);
        parse_method.set_x_pos(2);
        parse_method.set_y_pos(3);
        parse_method.set_uncertainty_sigma_pos(5);
        settings.set_parse_method(parse_method);
        Path exe_path = Paths.get("/some/where/fake_split");
        String default_a = Paths.get("/some/where/splitter_data/localisations_split_a").toString();
        String default_b = Paths.get("/some/where/splitter_data/localisations_split_b").toString();
        List<String> commands = Splitter.custom(exe_path).get_commands("something", settings);
        List<String> expected = Arrays.asList(exe_path.toString(), "--locs", "something", "-a", default_a,  "-b", default_b, "-m", "half", "--parse-method", "csv=10;,;2;3;5;-1;-1");
        assertEquals(expected, commands);
    }
}