#include "settings.hpp"

#include <catch2/catch_test_macros.hpp>

TEST_CASE("usable_threads_capped_at_system", "[settings, threads]")
{
	auto settings = hkmn::Settings::default_settings();
    settings.set_n_threads(100);
    CHECK(settings.usable_threads() < 100);
}
