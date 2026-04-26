#include "utils.hpp"

#include "squirrel_inner.hpp"

#include <catch2/catch_test_macros.hpp>

#include <opencv2/core.hpp>
#include <opencv2/core/hal/interface.h>

#include <format>
#include <iostream>


cv::Mat create_test_image(int size)
{    
    cv::Mat image;
    image.create(size, size, CV_64F);
    double count = 0.0;
    for (auto row = 0; row < size; row++)
    {
        for(auto col = 0; col < size; col++)
        {
            image.at<double>(row, col) = count++;
        }
    }
    // tu::display_image<double>(image);
    return image;
}

TEST_CASE("rmse", "[rmse]")
{
    const auto wf = create_test_image(10);
    const auto sr = wf + 1.0;
    const auto rmse = sqrl::image_rmse(wf, sr);
    // sqrt((1 * 100 ) / 100) = 1
    CHECK(rmse == 1.0);
}

TEST_CASE("pearsons_basic", "[pearsons]")
{
    const auto wf = create_test_image(10);
    const auto pearsons = sqrl::image_pearsons(wf, wf);
    CHECK(tu::within_tolerance(pearsons, 1.0, 0.00001));
}

TEST_CASE("pearsons_scale_invariance", "[pearsons]")
{
    const auto wf = create_test_image(10);
    const auto sr = wf + 1.0;
    const auto pearsons = sqrl::image_pearsons(wf, sr);
    CHECK(tu::within_tolerance(pearsons, 1.0, 0.00001));
}

TEST_CASE("pearsons_with_different_image_shapes", "[pearsons]")
{
    cv::Mat wf;
    wf.create(1, 10, CV_64F);
    wf.at<double>(0, 0) = 3.63;
    wf.at<double>(0, 1) = 3.02;
    wf.at<double>(0, 2) = 3.82;
    wf.at<double>(0, 3) = 3.42;
    wf.at<double>(0, 4) = 3.59;
    wf.at<double>(0, 5) = 2.87;
    wf.at<double>(0, 6) = 3.03;
    wf.at<double>(0, 7) = 3.46;
    wf.at<double>(0, 8) = 3.36;
    wf.at<double>(0, 9) = 3.3;

    cv::Mat sr;
    sr.create(10, 1, CV_64F);
    sr.at<double>(0, 0) = 53.1;
    sr.at<double>(0, 1) = 49.7;
    sr.at<double>(0, 2) = 48.4;
    sr.at<double>(0, 3) = 54.2;
    sr.at<double>(0, 4) = 54.9;
    sr.at<double>(0, 5) = 43.7;
    sr.at<double>(0, 6) = 47.2;
    sr.at<double>(0, 7) = 45.2;
    sr.at<double>(0, 8) = 54.4;
    sr.at<double>(0, 9) =  50.4;

    const auto pearsons = sqrl::image_pearsons(wf, sr);
    CHECK(tu::within_tolerance(pearsons, 0.47, 0.001));
}

TEST_CASE("affine", "[affine]")
{
    const auto size = 3;
    const auto image = create_test_image(size);
    const auto result = sqrl::affine_transform(image, 2, 1);
    cv::Mat expected = cv::Mat::zeros(size, size, CV_64F);
    expected.at<double>(0, 0) = 1;
    expected.at<double>(0, 1) = 3;
    expected.at<double>(0, 2) = 5;
    expected.at<double>(1, 0) = 7;
    expected.at<double>(1, 1) = 9;
    expected.at<double>(1, 2) = 11;
    expected.at<double>(2, 0) = 13;
    expected.at<double>(2, 1) = 15;
    expected.at<double>(2, 2) = 17;

    const auto ok = tu::images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("alpha_beta", "[alpha_beta]")
{
    const auto size = 3;
    const auto wf = create_test_image(size);
    const auto sr = create_test_image(size);
    const auto ones = cv::Mat::ones(size, size, CV_64F);
    const auto result = sqrl::calculate_alpha_beta_from_images(wf, sr, ones);
    CHECK(result->alpha == 1.0);
    CHECK(result->beta == 0.0);
}

TEST_CASE("downscale", "[downscale]")
{
    const auto sr = create_test_image(5);
    const auto result = sqrl::downscale(sr, cv::Size(3, 3));
    double expected_data[9] = {0.0, 1.0, 3.0, 5.0, 6.0, 8.0, 15.0, 16.0, 18.0};
    cv::Mat expected = cv::Mat(3, 3, CV_64F, expected_data);
    const auto ok = tu::images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("blur_and_downscale", "[blur, downscale]")
{
    const auto wf = create_test_image(3);
    auto sr = create_test_image(5);
    // sr.convertTo(sr, CV_64F);
    auto result = sqrl::blur_and_downscale(sr, 5.0, cv::Size(3, 3));
    // (*result).convertTo(*result, CV_64F);
    double expected_data[9] = {8.350920587290867, 8.649228497297534, 9.268972481520581, 9.842460137324181, 10.140768047330848, 10.760512031553898, 12.941180058439436, 13.239487968446102, 13.85923195266915};
    cv::Mat expected = cv::Mat(3, 3, CV_64F, expected_data);
    const auto ok = tu::images_equal<double, false>(*result, expected);
    if (!ok)
    {
        tu::show_differences<double>(*result, expected);
    }
    CHECK(ok);
}

TEST_CASE("error map", "[error_map]")
{
    const auto a = create_test_image(3);
    const auto b = create_test_image(3) + 1;
    auto result = sqrl::error_map(a, b, true);
    cv::Mat expected = cv::Mat::ones(3, 3, CV_64F) * -1.0;
    const auto ok = tu::images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("error map abs", "[error_map, abs]")
{
    const auto a = create_test_image(3);
    const auto b = create_test_image(3) + 1;
    auto result = sqrl::error_map(a, b, false);
    cv::Mat expected = cv::Mat::ones(3, 3, CV_64F);
    const auto ok = tu::images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("border", "[border]")
{
    cv::Mat image = cv::Mat::zeros(5, 5, CV_64F);
    image.at<double>(2, 2) = 1;
    image.at<double>(2, 3) = 1;
    auto border = sqrl::Border();
    sqrl::get_border(image, border);
    CHECK(border.top == 2);
    CHECK(border.left == 2);
    CHECK(border.right == 3);
    CHECK(border.bottom == 2);
}

TEST_CASE("border limits", "[border, limit]")
{
    cv::Mat image = cv::Mat::ones(3, 3, CV_64F);
    auto border = sqrl::Border();
    sqrl::get_border(image, border);
    CHECK(border.top == 0);
    CHECK(border.left == 0);
    CHECK(border.bottom == 2);
    CHECK(border.right == 2);
}

TEST_CASE("no border to crop", "[crop, border]")
{
    cv::Mat wf = cv::Mat::ones(3, 3, CV_64F);
    cv::Mat sr = cv::Mat::ones(5, 5, CV_64F);
    const double magnification = 5.0 / 3.0;
    const auto cropped = sqrl::crop_borders(wf, sr, magnification);
    CHECK(cropped.has_value() == false);
}

TEST_CASE("basic downstep index", "[downstep]")
{
    constexpr double MAGNIFICATION = 2;
    CHECK(sqrl::downstep_index(10, MAGNIFICATION) == 5);
    // ceiling
    CHECK(sqrl::downstep_index(11, MAGNIFICATION) == 6);
}

TEST_CASE("downstep with ceiling index", "[downstep]")
{
    constexpr double MAGNIFICATION = 2;    
    CHECK(sqrl::downstep_index(11, MAGNIFICATION) == 6);
}

TEST_CASE("rect based roi extraction", "[crop, roi]")
{
    cv::Mat image = create_test_image(5);
    cv::Rect rect_roi = cv::Rect(0, 1, 2, 2);
    cv::Mat roi = image(rect_roi);
    CHECK(roi.at<double>(0, 0) == 5.0);
    CHECK(roi.at<double>(0, 1) == 6.0);
    CHECK(roi.at<double>(1, 0) == 10.0);
    CHECK(roi.at<double>(1, 1) == 11.0);

    rect_roi = cv::Rect(1, 0, 2, 2);
    roi = image(rect_roi);
    CHECK(roi.at<double>(0, 0) == 1.0);
    CHECK(roi.at<double>(0, 1) == 2.0);
    CHECK(roi.at<double>(1, 0) == 6.0);
    CHECK(roi.at<double>(1, 1) == 7.0);
}

TEST_CASE("roi extraction", "[crop, roi]")
{
    cv::Mat image = create_test_image(10);
    for (auto col = 0; col < 10; col++)
    {
        image.at<double>(0, col) = 0;
        image.at<double>(1, col) = 0;
        image.at<double>(8, col) = 0;
        image.at<double>(9, col) = 0;
    }

    for (auto row = 0; row < 10; row++)
    {
        image.at<double>(row, 0) = 0;
        image.at<double>(row, 1) = 0;
        image.at<double>(row, 8) = 0;
        image.at<double>(row, 9) = 0;
    }
    cv::Rect rect_roi = cv::Rect(2, 2, 6, 6);
    cv::Mat roi = image(rect_roi);
    for (auto row = 0; row < roi.rows; row++)
    {
        for (auto col = 0; col < roi.cols; col++)
        {
            const auto value = roi.at<double>(row, col);
            // std::cout << std::format("Testing: {} {} = {}\n", row, col, value);
            CHECK(value > 0.0);
        }
    }
}

TEST_CASE("border crop scaled", "[border, crop]")
{
    cv::Mat wf_image = create_test_image(5);
    cv::Mat sr_image = create_test_image(10);
    for (auto col = 0; col < 10; col++)
    {
        sr_image.at<double>(0, col) = 0;
        sr_image.at<double>(1, col) = 0;
        sr_image.at<double>(8, col) = 0;
        sr_image.at<double>(9, col) = 0;
    }

    for (auto row = 0; row < 10; row++)
    {
        sr_image.at<double>(row, 0) = 0;
        sr_image.at<double>(row, 1) = 0;
        sr_image.at<double>(row, 8) = 0;
        sr_image.at<double>(row, 9) = 0;
    }
    const double magnification = 10 / 5;
    const auto cropped = sqrl::crop_borders(wf_image, sr_image, magnification);
    const cv::Mat& wf = std::get<0>(*cropped);
    const cv::Mat& sr = std::get<1>(*cropped);
    CHECK(sr.rows == 6);
    CHECK(sr.cols == 6);
    double count = 22.0;
    for (auto row = 0; row < 6; row++)
    {
        for (auto col = 0; col < 6; col++)
        {
            const auto value = sr.at<double>(row, col);
            CHECK(value == count);
            count += 1.0;
        }
        count += 4.0;
    }

    // tu::display_image<double>(wf);
    CHECK(wf.rows == 4);
    CHECK(wf.cols == 4);
    count = 6.0;
    for (auto row = 0; row < 4; row++)
    {
        for (auto col = 0; col < 4; col++)
        {
            const auto value = wf.at<double>(row, col);
            // std::cout << std::format("{} == {}\n", value, count);
            CHECK(value == count);
            count += 1.0;
        }
        count += 1.0;
    }
}

TEST_CASE("get patches", "[patches, tiling]")
{
    cv::Mat image = create_test_image(10);
    auto settings = sqrl::Settings::default_settings();
    settings.set_patch_size(3);
    settings.set_step_size(2);
    const auto patches = sqrl::get_patches_over(image, settings);
    CHECK(patches.size() == 16);
    CHECK(patches[0].is_equal(sqrl::Patch::from(0, 3, 0, 3)));
    CHECK(patches[1].is_equal(sqrl::Patch::from(0, 3, 2, 5)));
    CHECK(patches[2].is_equal(sqrl::Patch::from(0, 3, 4, 7)));
    CHECK(patches[3].is_equal(sqrl::Patch::from(0, 3, 6, 9)));
    CHECK(patches[4].is_equal(sqrl::Patch::from(2, 5, 0, 3)));
    CHECK(patches[5].is_equal(sqrl::Patch::from(2, 5, 2, 5)));
    CHECK(patches[6].is_equal(sqrl::Patch::from(2, 5, 4, 7)));
    CHECK(patches[7].is_equal(sqrl::Patch::from(2, 5, 6, 9)));
    CHECK(patches[8].is_equal(sqrl::Patch::from(4, 7, 0, 3)));
    CHECK(patches[9].is_equal(sqrl::Patch::from(4, 7, 2, 5)));
    CHECK(patches[10].is_equal(sqrl::Patch::from(4, 7, 4, 7)));
    CHECK(patches[11].is_equal(sqrl::Patch::from(4, 7, 6, 9)));
    CHECK(patches[12].is_equal(sqrl::Patch::from(6, 9, 0, 3)));
    CHECK(patches[13].is_equal(sqrl::Patch::from(6, 9, 2, 5)));
    CHECK(patches[14].is_equal(sqrl::Patch::from(6, 9, 4, 7)));
    CHECK(patches[15].is_equal(sqrl::Patch::from(6, 9, 6, 9)));
}

TEST_CASE("patch centre", "[patches, map]")
{
    const auto patch = sqrl::Patch::from(0, 3, 0, 3);
    CHECK(patch.centre_row() == 1);
    CHECK(patch.centre_col() == 1);
}