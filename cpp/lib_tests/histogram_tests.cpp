#include "imp.hpp"

#include "utils.hpp"

#include <catch2/catch_test_macros.hpp>

TEST_CASE("histogram", "[histogram]")
{
	auto image = tu::create_test_image(10);
	double max_value;
	const auto hist = imp::generate_histogram<double>(image, 10, &max_value);
    CHECK(max_value == 99);
    CHECK(hist[0] == 9); // we are 1 short because zero values are discarded as per Richard
    CHECK(hist[1] == 10);
    CHECK(hist[2] == 10);
    CHECK(hist[3] == 10);
    CHECK(hist[4] == 10);
    CHECK(hist[5] == 10);
    CHECK(hist[6] == 10);
    CHECK(hist[7] == 10);
    CHECK(hist[8] == 10);
    CHECK(hist[9] == 10);
}

TEST_CASE("cumulative_sum", "[histogram]")
{
	auto image = tu::create_test_image(10);
	double max_value;
	const auto hist = imp::generate_histogram<double>(image, 10, &max_value);
	const auto cum_sum = imp::cumulative_sum(hist);
	CHECK(cum_sum[0] == 9);
	CHECK(cum_sum[1] == 19);
	CHECK(cum_sum[2] == 29);
	CHECK(cum_sum[3] == 39);
	CHECK(cum_sum[4] == 49);
	CHECK(cum_sum[5] == 59);
	CHECK(cum_sum[6] == 69);
	CHECK(cum_sum[7] == 79);
	CHECK(cum_sum[8] == 89);
	CHECK(cum_sum[9] == 99);
}

TEST_CASE("threshold_bin_number", "[histogram]")
{
	constexpr size_t n_bins = 10;
	auto image = tu::create_test_image(10);
	double max_value;
	const auto hist = imp::generate_histogram<double>(image, n_bins, &max_value);
	const auto cum_sum = imp::cumulative_sum(hist);
	const auto bin_number = imp::threshold_bin_number(hist, cum_sum);
	CHECK(bin_number == 9);
	const auto threshold_value = imp::threshold_value(max_value, bin_number, n_bins);
	CHECK(threshold_value == 89.10000000000000853);
}

TEST_CASE("threshold_value", "[histogram]")
{
	constexpr size_t N_BINS = 10;
	constexpr double MAX_VALUE = 10.0;
	CHECK(imp::threshold_value(MAX_VALUE, 0, N_BINS) == 0.0);
	CHECK(imp::threshold_value(MAX_VALUE, 1, N_BINS) == 1.0);
	CHECK(imp::threshold_value(MAX_VALUE, 2, N_BINS) == 2.0);
	CHECK(imp::threshold_value(MAX_VALUE, 3, N_BINS) == 3.0);
	CHECK(imp::threshold_value(MAX_VALUE, 4, N_BINS) == 4.0);
	CHECK(imp::threshold_value(MAX_VALUE, 5, N_BINS) == 5.0);
	CHECK(imp::threshold_value(MAX_VALUE, 6, N_BINS) == 6.0);
	CHECK(imp::threshold_value(MAX_VALUE, 7, N_BINS) == 7.0);
	CHECK(imp::threshold_value(MAX_VALUE, 8, N_BINS) == 8.0);
	CHECK(imp::threshold_value(MAX_VALUE, 9, N_BINS) == 9.0);
}

TEST_CASE("threshold_value_256", "[histogram]")
{
	constexpr size_t n_bins = 256;
	auto image = tu::create_test_image(10);
	double max_value;
	const auto hist = imp::generate_histogram<double>(image, n_bins, &max_value);
	const auto cum_sum = imp::cumulative_sum(hist);
	const auto bin_number = imp::threshold_bin_number(hist, cum_sum);
	CHECK(bin_number == 253);
	const auto threshold_value = imp::threshold_value(max_value, bin_number, n_bins);
	CHECK(threshold_value == 97.83984375);
}

TEST_CASE("flatten_at", "[histogram]")
{
	constexpr size_t n_bins = 256;
	auto image = tu::create_test_image(10);
	const auto flat_image = imp::flatten_image_at<double>(image, 19);
	auto expected = tu::create_test_image(10);
	for (auto row = 2; row < 10; row++)
	{
		for(auto col = 0; col < 10; col++)
		{
			expected.at<double>(row, col) = 19;
		}
	}
	CHECK(tu::images_equal<double>(flat_image, expected));
}