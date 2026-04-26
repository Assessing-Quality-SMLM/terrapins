#include "cli.hpp"

#include <catch2/catch_test_macros.hpp>

#include <iostream>
TEST_CASE("read_setting", "[cli, read]")
{
	const auto cli = hkmn::CommandLine{"some=thing"};
    const auto setting = hkmn::get_setting(cli, "some");
    CHECK(*setting == "thing");
}

TEST_CASE("read_fails", "[cli, fail]")
{
    const auto cli = hkmn::CommandLine{"some=thing"};
    const auto setting = hkmn::get_setting(cli, "else");
    CHECK(setting.has_value() == false);
}

TEST_CASE("read_double", "[cli, parse]")
{
    const auto cli = hkmn::CommandLine{"some=10.2"};
    const auto setting = hkmn::get_setting_double(cli, "some");
    CHECK(*setting == 10.2);
}

TEST_CASE("read_int", "[cli, parse]")
{
    const auto cli = hkmn::CommandLine{"some=123"};
    const auto setting = hkmn::get_setting_int(cli, "some");
    CHECK(*setting == 123);
}

TEST_CASE("read_threshold", "[cli, parse]")
{
    const auto cli = hkmn::CommandLine{"some=12.3, 4.56,6.78"};
    const auto setting = hkmn::get_setting_threshold(cli, "some");
    CHECK(setting->threshold() == 12.3);
    CHECK(setting->smoothing() == 4.56);
    CHECK(setting->offset() == 6.78);
}

TEST_CASE("read_n_threads", "[cli, parse]")
{
    const auto cli = hkmn::CommandLine{"threads=3"};
    const auto setting = hkmn::get_setting_uint8_t(cli, "threads");
    CHECK(*setting == 3);
}

TEST_CASE("read_n_threads_fail_overflow", "[cli, parse, fail]")
{
    const auto cli = hkmn::CommandLine{"threads=300"};
    const auto setting = hkmn::get_setting_uint8_t(cli, "threads");
    CHECK(setting.has_value() == false);
}

TEST_CASE("read_n_threads_fail_underflow", "[cli, parse, fail]")
{
    const auto cli = hkmn::CommandLine{"threads=-1"};
    const auto setting = hkmn::get_setting_uint8_t(cli, "threads");
    CHECK(setting.has_value() == false);
}

TEST_CASE("option_set", "[cli, option_set]")
{
    const auto cli = hkmn::CommandLine{"some", "thing"};
    CHECK(hkmn::option_set(cli, "some"));
    CHECK(hkmn::option_set(cli, "thing"));
    CHECK(!hkmn::option_set(cli, "else"));
}