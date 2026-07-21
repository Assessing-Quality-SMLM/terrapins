#include "cli.hpp"

#include "threshold_settings.hpp"

#include <algorithm>
#include <string>
#include <sstream>

#include <format>
#include <iostream>

namespace hkmn
{
    std::vector<std::string> parse_csv(const std::string& s)
    {
        std::stringstream ss(s);
        std::vector<std::string> result;

        while(ss.good())
        {
            std::string substr;
            std::getline(ss, substr, ',' );
            result.push_back(substr);
        }
        return result;
    }

    std::optional<double> parse_double(const std::string& s)
    {
        try
        {
            const auto value = std::stod(s);
            return value;
        }
        catch (const std::invalid_argument& ia) 
        {
            return {};
        }
    }

    std::optional<int> parse_int(const std::string& s)
    {
        try
        {
            const auto value = std::stoi(s);
            return value;
        }
        catch (const std::invalid_argument& ia) 
        {
            return {};
        }
    }

    std::optional<ThresholdSettings> parse_threshold_settings(const std::string& s)
    {
        auto settings = ThresholdSettings::default_settings();
        const auto values = parse_csv(s);
        if (values.size() < 3)
            return {};
        auto setting = parse_double(values[0]);
        if (!setting)
            return {};
        settings.set_threshold(*setting);
        setting = parse_double(values[1]);
        if (!setting)
            return {};
        settings.set_smoothing(*setting);
        setting = parse_double(values[2]);
        if (!setting)
            return {};
        settings.set_offset(*setting);
        return settings;
    }   

    std::optional<std::string> get_setting(const CommandLine& command_line, const std::string& key)
    {
        // for (const auto& x : command_line)
        //     std::cout << x << "\n";
        const auto f = [&](const auto& s){return s.rfind(key, 0) == 0;};
        const auto it = std::find_if(command_line.begin(), command_line.end(), f);
        if (it != command_line.end())
        {
            const auto tmp = *it;
            const auto position = tmp.rfind("=");
            if (position == std::string::npos || position >= tmp.size())
            {
                return {};
            }
            return tmp.substr(position + 1);
        }
        return {};
    }

    std::optional<double> get_setting_double(const CommandLine& command_line, const std::string& key)
    {
        return get_setting(command_line, key).and_then(parse_double);
    }

    std::optional<int> get_setting_int(const CommandLine& command_line, const std::string& key)
    {
        return get_setting(command_line, key).and_then(parse_int);
    }

    std::optional<std::uint8_t> int_to_uint8_t(const int value)
    {
        if (value >= 0 && value <= 255)
            return (std::uint8_t)value;
        return {};
    }

    std::optional<std::uint8_t> get_setting_uint8_t(const CommandLine& command_line, const std::string& key)
    {
        return get_setting_int(command_line, key).and_then(int_to_uint8_t);
    }

    bool option_set(const CommandLine& command_line, const std::string& key)
    {
        const auto f = [&](const auto& s){return s.rfind(key, 0) == 0;};
        const auto it = std::find_if(command_line.begin(), command_line.end(), f);
        return it != command_line.end();
    }

    std::optional<ThresholdSettings> get_setting_threshold(const CommandLine& command_line, const std::string& key)
    {
        return get_setting(command_line, key).and_then(parse_threshold_settings);
    }

    void build_command_line_into(int argc, char const* argv[], CommandLine& command_line)
    {
        for (auto idx = 0; idx < argc; idx++)
            command_line.emplace_back(argv[idx]);
    }

    CommandLine build_command_line(int argc, char const* argv[])
    {
        auto command_line = CommandLine();
        build_command_line_into(argc, argv, command_line);
        return command_line;
    }

    Settings get_settings_from(const CommandLine& command_line)
    {        
        auto settings = hkmn::Settings::default_settings();

        const auto ref_image = get_setting(command_line, "ref");
        if (ref_image)
            settings.set_ref_image(*ref_image);

        const auto test_image = get_setting(command_line, "test");
        if (test_image)
            settings.set_test_image(*test_image);

        const auto start_level = get_setting_uint8_t(command_line, "start");
        if (start_level)
            settings.set_start_level(*start_level);

        const auto n_levels = get_setting_uint8_t(command_line, "n");
        if (n_levels)
            settings.set_n_levels(*n_levels);

        const auto psf = get_setting_double(command_line, "psf");
        if (psf)
            settings.set_psf(*psf);

        const auto dliate_erode = option_set(command_line, "dilate-erode");
        if (dliate_erode)
            settings.set_dilate_erode(true);

        const auto n_threads = get_setting_uint8_t(command_line, "threads");
        if (n_threads)
            settings.set_n_threads(*n_threads);

        const auto fwhm_settings = get_setting_threshold(command_line, "fwhm");
        if (fwhm_settings)
            settings.set_fwhm_threshold_settings(*fwhm_settings);

        const auto skel_settings = get_setting_threshold(command_line, "skel");
        if (skel_settings)
            settings.set_skeleton_threshold_settings(*skel_settings);

        const auto score_threshold = get_setting_double(command_line, "score-threshold");
        if (score_threshold)
            settings.set_score_threshold(*score_threshold);

        const auto output_directory = get_setting(command_line, "o");
        if (output_directory)
            settings.set_output_directory(*output_directory);

        const auto do_not_flatten = option_set(command_line, "no-flat");
        if (do_not_flatten)
        {
            settings.set_flatten_intensities(false);
        }
        else
        {
            settings.set_flatten_intensities(true);
        }

        return settings;
    }

    Settings get_settings(int argc, char const* argv[])
    {
        auto command_line = build_command_line(argc, argv);
        return get_settings_from(command_line);
    }
}

namespace sqrl
{
    Settings get_settings(int argc, char const* argv[])
    {
        auto command_line = hkmn::CommandLine();
        for (auto idx = 0; idx < argc; idx++)
            command_line.emplace_back(argv[idx]);

        auto settings = Settings::default_settings();

        const auto wf_image = hkmn::get_setting(command_line, "wf");
        if (wf_image)
            settings.set_wf_image(*wf_image);

        const auto sr_image = hkmn::get_setting(command_line, "sr");
        if (sr_image)
            settings.set_sr_image(*sr_image);

        const auto output_directory = hkmn::get_setting(command_line, "od");
        if (output_directory)
            settings.set_output_directory(*output_directory);

        const auto pixel_size = hkmn::get_setting_double(command_line, "px");
        if (pixel_size)
            settings.set_pixel_size_nm(*pixel_size);

        const auto sigma = hkmn::get_setting_double(command_line, "sigma");
        if (sigma)
            settings.set_sigma_nm(*sigma);

        const auto show_pos_neg = hkmn::option_set(command_line, "pn");
        settings.set_show_positive_negative(show_pos_neg);

        const auto do_registration = hkmn::option_set(command_line, "reg");
        settings.set_registration(do_registration);

        const auto registration_method = hkmn::get_setting(command_line, "reg_meth");
        if (registration_method)
            settings.set_registration_method(*registration_method);

        const auto crop_border = hkmn::get_setting_int(command_line, "wfb");
        if (crop_border)
            settings.set_wf_border_size(*crop_border);

        const auto write_optimiser_data = hkmn::option_set(command_line, "wo");
        settings.set_write_optimiser_data(write_optimiser_data);

        const auto show_algorithms = hkmn::option_set(command_line, "opt_show");
        settings.set_display_algorithms(show_algorithms);

        const auto optimisation_algorithm = hkmn::get_setting(command_line, "opt");
        if (optimisation_algorithm)
            settings.set_optimiser_algorithm(*optimisation_algorithm);

        const auto three_parameter_solve = hkmn::option_set(command_line, "3p");
        settings.set_three_parameter_solve(three_parameter_solve);

        const auto patchwise = hkmn::option_set(command_line, "pw");
        settings.set_patchwise(patchwise);

        const auto patch_size = hkmn::get_setting_int(command_line, "ps");
        if (patch_size)
            settings.set_patch_size(*patch_size);

        const auto step_size = hkmn::get_setting_int(command_line, "ss");
        if (step_size)
            settings.set_step_size(*step_size);

        const auto mt = hkmn::option_set(command_line, "mt");
        settings.set_mt(mt);

        const auto n_threads = hkmn::get_setting_uint8_t(command_line, "nt");
        if (n_threads)
            settings.set_n_threads(*n_threads);
        
        return settings;
    }   
}