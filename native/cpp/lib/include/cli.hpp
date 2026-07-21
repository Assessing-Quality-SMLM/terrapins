#ifndef CLI_HPP_
#define CLI_HPP_

#include "settings.hpp"
#include "squirrel_settings.hpp"

#include <optional>
#include <string>
#include <vector>
namespace hkmn 
{
    using CommandLine = std::vector<std::string>;
    std::optional<ThresholdSettings> get_setting_threshold(const CommandLine& command_line, const std::string& key);
    std::optional<double> get_setting_double(const CommandLine& command_line, const std::string& key);
    std::optional<int> get_setting_int(const CommandLine& command_line, const std::string& key);
    std::optional<std::uint8_t> get_setting_uint8_t(const CommandLine& command_line, const std::string& key);
    std::optional<std::string> get_setting(const CommandLine& command_line, const std::string& key);
    bool option_set(const CommandLine& command_line, const std::string& key);
    void build_command_line_into(int argc, char const* argv[], CommandLine& command_line);
    CommandLine build_command_line(int argc, char const* argv[]);
	Settings get_settings_from(const CommandLine& command_line);
    Settings get_settings(int argc, char const* argv[]);
}

namespace sqrl
{
    Settings get_settings(int argc, char const* argv[]);
}
#endif //CLI_HPP_