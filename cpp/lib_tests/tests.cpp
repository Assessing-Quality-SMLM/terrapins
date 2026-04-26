#include "hawkman_inner.hpp"
#include "hue_calculator.hpp"
#include "imp.hpp"
#include "utils.hpp"

#include <catch2/catch_test_macros.hpp>

#include <cstdint>
#include <opencv2/core.hpp>
#include <opencv2/core/hal/interface.h>

#include <format>
#include <iostream>

cv::Mat create_binary_test_image(int size, int width, unsigned char true_value)
{    
    cv::Mat image = cv::Mat::zeros(size, size, CV_8U);
    int centre = size / 2;
    int half = width / 2;
    int start = centre - half;
    int end = start + width;
    for (auto row = 0; row < size; row++)
    {
        for(auto col = start; col < end; col++)
        {
            image.at<unsigned char>(row, col) = true_value;
        }
    }
    // tu::display_image(image);
    return image;
}

cv::Mat normalised_image()
{
    cv::Mat image;
    image.create(4, 4, CV_64F);
    double count = 0.0;
    for (auto row = 0; row < 4; row++)
    {
        for(auto col = 0; col < 4; col++)
        {
            image.at<double>(row, col) = count++ / 15;
        }
    }
    // tu::display_image(image);
    return image;
}

template<typename T, bool EXACT = true>
bool images_equal(const cv::Mat& image_1, const cv::Mat& image_2)
{
    return tu::show_differences<T, EXACT>(image_1, image_2);
}

TEST_CASE("tolerance", "[tolerance]")
{
    CHECK(tu::within_tolerance(0.0, 0.01, 0.1));
    CHECK(!tu::within_tolerance(0.0, 0.1, 0.01));
    CHECK(tu::within_tolerance(0.0, 0.1, 0.1));
}

TEST_CASE("Get Max", "[max]")
{
    const auto image = tu::create_test_image(10);
    CHECK(99 == imp::get_max(image));
}

TEST_CASE("Histogram", "[hist, histogram , flattening]")
{
    const auto image = tu::create_test_image(10);
    double max_value;
    const auto hist = imp::generate_histogram<double>(image, 10, &max_value);
    CHECK(99 == max_value);
    CHECK(hist[0] == 9);
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

TEST_CASE("Flatten", "[flattening]")
{
    const auto image = tu::create_test_image(10);
    const auto flat_image = imp::flatten<double>(image);
    // tu::display_image<double, double>(flat_image);
    auto it = flat_image.begin<double>();
    std::advance(it, 98);
    for (; it != flat_image.end<double>(); it++)
        CHECK(*it == 97.83984375);
}

TEST_CASE("Half Psf Kernel Size", "[psf, half_psf]")
{
    CHECK(7 == imp::half_psf_kernel_size(13));
}

TEST_CASE("Normalise Image", "[norm, image]")
{
    auto test = tu::create_test_image(4);
    auto norm = normalised_image();
    imp::normalise_image(test);
    CHECK(cv::sum(test - norm)[0] == 0.0);
}

TEST_CASE("Blur To Half PSF", "[psf, half_psf, blur, convolve]")
{
    int size = 10;
    auto test = tu::create_test_image(size);
    auto result = imp::half_psf_blur_cv(test, 13);
    // tu::display_image(result);
    double expected_data[100] = {0.09523809523809526, 0.10101010101010101, 0.10822510822510822, 0.1168831168831169, 0.12698412698412698, 0.13708513708513706, 0.1471861471861472, 0.15584415584415584, 0.16305916305916307, 0.16883116883116883, 0.152958152958153, 0.15873015873015872, 0.16594516594516592, 0.17460317460317462, 0.18470418470418473, 0.1948051948051948, 0.20490620490620493, 0.21356421356421354, 0.2207792207792208, 0.22655122655122653, 0.22510822510822512, 0.2308802308802309, 0.2380952380952381, 0.24675324675324675, 0.25685425685425683, 0.26695526695526695, 0.27705627705627706, 0.2857142857142857, 0.2929292929292929, 0.29870129870129863, 0.3116883116883117, 0.3174603174603175, 0.3246753246753247, 0.33333333333333337, 0.3434343434343434, 0.35353535353535354, 0.36363636363636365, 0.37229437229437223, 0.37950937950937946, 0.3852813852813853, 0.4126984126984127, 0.4184704184704185, 0.42568542568542567, 0.4343434343434344, 0.4444444444444444, 0.4545454545454546, 0.46464646464646464, 0.4733044733044733, 0.48051948051948046, 0.4862914862914863, 0.5137085137085137, 0.5194805194805194, 0.5266955266955267, 0.5353535353535354, 0.5454545454545455, 0.5555555555555556, 0.5656565656565656, 0.5743145743145743, 0.5815295815295815, 0.5873015873015873, 0.6147186147186147, 0.6204906204906205, 0.6277056277056277, 0.6363636363636365, 0.6464646464646465, 0.6565656565656566, 0.6666666666666667, 0.6753246753246753, 0.6825396825396824, 0.6883116883116884, 0.7012987012987012, 0.7070707070707071, 0.7142857142857142, 0.722943722943723, 0.733044733044733, 0.7431457431457432, 0.7532467532467533, 0.7619047619047619, 0.769119769119769, 0.774891774891775, 0.7734487734487733, 0.7792207792207793, 0.7864357864357864, 0.7950937950937952, 0.8051948051948051, 0.8152958152958153, 0.8253968253968255, 0.834054834054834, 0.8412698412698411, 0.8470418470418472, 0.8311688311688309, 0.8369408369408369, 0.8441558441558441, 0.8528138528138529, 0.8629148629148627, 0.8730158730158731, 0.8831168831168832, 0.8917748917748918, 0.8989898989898987, 0.9047619047619049};
    cv::Mat expected = cv::Mat(size, size, CV_64F, expected_data);
    const auto ok = images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Gaussian Blur", "[gaussian, gauss, blur]")
{
    int size = 10;
    auto test = tu::create_test_image(size);
    auto result = imp::gaussian_blur_cv(test, 2.355, cv::BORDER_REPLICATE);
    double expected_data[100] = {10.177295911833442, 10.761862896742247, 11.500700473474508, 12.35561014257686, 13.279177811919107, 14.224996573232607, 15.148564242574855, 16.003473911677208, 16.742311488409474, 17.326878473318267, 16.02296576092147, 16.607532745830273, 17.346370322562528, 18.201279991664876, 19.12484766100713, 20.070666422320635, 20.99423409166289, 21.849143760765234, 22.587981337497492, 23.172548322406296, 23.411341528244083, 23.995908513152884, 24.734746089885146, 25.589655758987487, 26.51322342832974, 27.459042189643245, 28.382609858985493, 29.237519528087848, 29.9763571048201, 30.560924089728907, 31.96043821926759, 32.54500520417639, 33.28384278090866, 34.13875245001101, 35.062320119353245, 36.00813888066676, 36.931706550009004, 37.78661621911136, 38.52545379584362, 39.11002078075243, 41.19611491269009, 41.78068189759889, 42.519519474331155, 43.37442914343352, 44.29799681277574, 45.24381557408926, 46.16738324343151, 47.02229291253386, 47.76113048926611, 48.345697474174926, 50.6543025258251, 51.2388695107339, 51.97770708746617, 52.83261675656853, 53.75618442591076, 54.70200318722428, 55.625570856566526, 56.480480525668874, 57.219318102401125, 57.80388508730994, 59.88997921924761, 60.474546204156404, 61.21338378088868, 62.068293449991025, 62.99186111933326, 63.937679880646776, 64.86124754998902, 65.71615721909137, 66.45499479582365, 67.03956178073244, 68.43907591027113, 69.0236428951799, 69.76248047191217, 70.61739014101454, 71.5409578103568, 72.48677657167029, 73.41034424101254, 74.26525391011488, 75.00409148684714, 75.58865847175595, 75.82745167759374, 76.41201866250252, 77.15085623923478, 78.00576590833714, 78.9293335776794, 79.87515233899288, 80.79872000833512, 81.6536296774375, 82.39246725416974, 82.97703423907856, 81.67312152668175, 82.25768851159053, 82.99652608832281, 83.85143575742515, 84.77500342676744, 85.72082218808092, 86.64438985742316, 87.49929952652553, 88.23813710325777, 88.82270408816657};
    cv::Mat expected = cv::Mat(size, size, CV_64F, expected_data);
    const auto ok = images_equal<double, false>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Gaussian Blur and normalise", "[gaussian, gauss, blur, normalise]")
{
    int size = 10;
    auto test = tu::create_test_image(size);
    auto result = imp::gaussian_blur_and_normalise_cv(test, 2.355);
    double expected_data[100] = {0.11457989279105175, 0.12116117165336332, 0.12947928788633548, 0.13910418816243783, 0.14950206648446573, 0.1601504561166331, 0.170548334438661, 0.18017323471476335, 0.18849135094773556, 0.195072629810047, 0.18039268141416706, 0.1869739602764786, 0.1952920765094507, 0.204916976785553, 0.21531485510758103, 0.22596324473974838, 0.2363611230617764, 0.24598602333787864, 0.2543041395708508, 0.2608854184331623, 0.2635738437438887, 0.2701551226062002, 0.2784732388391724, 0.2880981391152746, 0.29849601743730264, 0.30914440706947, 0.3195422853914979, 0.3291671856676003, 0.33748530190057235, 0.34406658076288393, 0.3598228465049121, 0.3664041253672236, 0.3747222416001958, 0.3843471418762982, 0.39474502019832597, 0.4053934098304935, 0.4157912881525213, 0.4254161884286237, 0.43373430466159585, 0.4403155835239075, 0.4638016297251915, 0.470382908587503, 0.4787010248204752, 0.4883259250965777, 0.49872380341860534, 0.5093721930507729, 0.5197700713728008, 0.5293949716489031, 0.5377130878818752, 0.5442943667441869, 0.570285526046865, 0.5768668049091764, 0.5851849211421487, 0.5948098214182511, 0.6052076997402789, 0.6158560893724464, 0.6262539676944743, 0.6358788679705767, 0.6441969842035487, 0.6507782630658604, 0.6742643092671444, 0.680845588129456, 0.6891637043624282, 0.6987886046385305, 0.7091864829605583, 0.7198348725927258, 0.7302327509147537, 0.739857651190856, 0.7481757674238284, 0.7547570462861398, 0.770513312028168, 0.7770945908904793, 0.7854127071234515, 0.795037607399554, 0.8054354857215821, 0.8160838753537493, 0.8264817536757771, 0.8361066539518794, 0.8444247701848516, 0.8510060490471631, 0.8536944743578896, 0.8602757532202009, 0.8685938694531731, 0.8782187697292755, 0.8886166480513036, 0.8992650376834705, 0.9096629160054985, 0.9192878162816011, 0.9276059325145731, 0.9341872113768848, 0.9195072629810048, 0.926088541843316, 0.9344066580762884, 0.9440315583523906, 0.954429436674419, 0.965077826306586, 0.975475704628614, 0.9851006049047165, 0.9934187211376884, 1.0};
    cv::Mat expected = cv::Mat(size, size, CV_64F, expected_data);
    const auto ok = images_equal<double, false>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Blur to scale", "[blur]")
{
    int size = 10;
    auto test = tu::create_test_image(size);
    auto result = hkmn::blur_to_scale(test, 3);
    double expected_data[100] = {3.6666666666666665, 4.333333333333333, 5.333333333333333, 6.333333333333333, 7.333333333333333, 8.333333333333332, 9.333333333333332, 10.333333333333332, 11.333333333333332, 12, 10.333333333333332, 11, 12, 13, 14, 15, 16, 17, 18, 18.666666666666664, 20.333333333333332, 21, 22, 23, 24, 25, 26, 27, 28, 28.666666666666664, 30.333333333333332, 31, 32, 33, 34, 35, 36, 37, 38, 38.666666666666664, 40.33333333333333, 41, 42, 43, 44, 45, 46, 47, 48, 48.666666666666664, 50.33333333333333, 51, 52, 53, 54, 55, 56, 57, 58, 58.666666666666664, 60.33333333333333, 61, 62, 63, 64, 65, 66, 67, 68, 68.66666666666666, 70.33333333333333, 71, 72, 73, 74, 75, 76, 77, 78, 78.66666666666666, 80.33333333333333, 81, 82, 83, 84, 85, 86, 87, 88, 88.66666666666666, 87, 87.66666666666666, 88.66666666666666, 89.66666666666666, 90.66666666666666, 91.66666666666666, 92.66666666666666, 93.66666666666666, 94.66666666666666, 95.33333333333333};
    cv::Mat expected = cv::Mat(size, size, CV_64F, expected_data);
    const auto ok = images_equal<double>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Thresholding", "[threshold, thresh]")
{
    int size = 10;
    auto half_psf = tu::create_test_image(size);
    auto gauss_blur = tu::create_test_image(size);
    auto scale_blur = tu::create_test_image(size);
    auto settings = hkmn::ThresholdSettings::default_settings();
    settings.set_threshold(1.0);
    settings.set_smoothing(1.0);
    settings.set_offset(-1.0);
    auto result = hkmn::threshold_image(gauss_blur, scale_blur, half_psf, settings);
    unsigned char expected_data[100] = {255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    cv::Mat expected = cv::Mat(size, size, CV_8U, expected_data);
    const auto ok = images_equal<unsigned char>(result, expected);
    if (!ok)
    {
        tu::show_differences<unsigned char>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Erosion", "[erode, morphology, morph]")
{
    int size = 10;
    auto image = create_binary_test_image(size, 5, 255);
    // tu::display_image<unsigned char>(image);
    auto result = imp::erode(image, 3, cv::BORDER_ISOLATED);
    // std::cout << "RESULT\n";
    // tu::display_image<unsigned char>(image);
    unsigned char expected_data[100] = {0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0, 0};
    cv::Mat expected = cv::Mat(size, size, CV_8U, expected_data);
    const auto ok = images_equal<unsigned char>(image, expected);
    if (!ok)
    {
        tu::show_differences<unsigned char>(image, expected);
    }
    CHECK(ok);
}

TEST_CASE("Dilation", "[dilate, morphology, morph]")
{
    int size = 10;
    auto image = create_binary_test_image(size, 5, 255);
    auto result = imp::dilate(image, 3, cv::BORDER_ISOLATED);
    unsigned char expected_data[100] = {0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 255, 255, 255, 255, 255, 255, 255, 0};
    cv::Mat expected = cv::Mat(size, size, CV_8U, expected_data);
    const auto ok = images_equal<unsigned char>(image, expected);
    if (!ok)
    {
        tu::show_differences<unsigned char>(image, expected);
    }
    CHECK(ok);
}

// TEST_CASE("Skeletonise", "[skeleton, morphology, morph]")
// {
//     int size = 10;
//     auto image = create_binary_test_image(size, 5, 255);
//     tu::display_image<unsigned char>(image);
//     auto result = hkmn::skeletonise(image);
//     std::cout << "Result\n";
//     tu::display_image<unsigned char>(image);
//     unsigned char expected_data[100] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//     cv::Mat expected = cv::Mat(size, size, CV_8U, expected_data);
//     std::cout << "Expected\n";
//     tu::display_image<unsigned char>(expected);
//     const auto ok = images_equal<unsigned char>(image, expected);
//     if (!ok)
//     {
//         tu::show_differences<unsigned char>(image, expected);
//     }
//     CHECK(ok);
// }

TEST_CASE("Correlate Patch", "[correlate]")
{
    double data_1[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
    double data_2[] = {0.0, 1.0, 1.0, 3.0, 1.0, 5.0};
    cv::Mat image_1 = cv::Mat(3, 2, CV_64F, data_1);
    cv::Mat image_2 = cv::Mat(3, 2, CV_64F, data_2);
    auto result = imp::correlate<double>(image_1, image_2);
    CHECK(result == 0.78655606187506977); //not indentical to imageJ version
}

TEST_CASE("Correlate Map", "[correlate]")
{
    double data_1[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
    double data_2[] = {0.0, 1.0, 1.0, 3.0, 1.0, 5.0};
    cv::Mat image_1 = cv::Mat(3, 2, CV_64F, data_1);
    cv::Mat image_2 = cv::Mat(3, 2, CV_64F, data_2);
    auto result = imp::correlation_map<double>(image_1, image_2, 1);
    double expected_data[] = {0.9557790087219502, 0.95577900872195, 0.8526687663783824, 0.8526687663783825, 0.8631766282897236, 0.8631766282897236};
    cv::Mat expected = cv::Mat(3, 2, CV_64F, expected_data);
    const auto ok = tu::images_equal<double, false>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Correlate Map Integral", "[correlate]")
{
    double data_1[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
    double data_2[] = {0.0, 1.0, 1.0, 3.0, 1.0, 5.0};
    cv::Mat image_1 = cv::Mat(3, 2, CV_64F, data_1);
    cv::Mat image_2 = cv::Mat(3, 2, CV_64F, data_2);
    auto result = imp::correlation_map_integral(image_1, image_2, 1);
    double expected_data[] = {0.9557790087219502, 0.95577900872195, 0.8526687663783824, 0.8526687663783825, 0.8631766282897236, 0.8631766282897236};
    cv::Mat expected = cv::Mat(3, 2, CV_64F, expected_data);
    const auto ok = tu::images_equal<double, false>(result, expected);
    if (!ok)
    {
        tu::show_differences<double>(result, expected);
    }
    CHECK(ok);
}

// TEST_CASE("Confidence Map", "[confidence_map]")
// {
//     double data_1[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
//     double data_2[] = {0.0, 1.0, 1.0, 3.0, 1.0, 5.0};
//     cv::Mat image_1 = tu::create_test_image(10);
//     cv::Mat image_2 = tu::create_test_image(10);
//     cv::Mat skeleton = create_binary_test_image(10, 5, 255);
//     cv::Mat confidence_map = create_binary_test_image(10, 5, 255);
//     auto result = hkmn::create_colour_confidence_map(image_1, image_2, skeleton, confidence_map);
//     cv::Mat result_a;
//     cv::Mat result_b;
//     cv::extractChannel(result, result_a, 0);
//     cv::extractChannel(result, result_b, 1);
//     unsigned char expected_data_a[] = {0, 254, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255};
//     unsigned char expected_data_b[] = {0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 255, 255, 255, 255, 255, 0, 0};
//     cv::Mat expected_a = cv::Mat(10, 10, CV_8U, expected_data_a);
//     cv::Mat expected_b = cv::Mat(10, 10, CV_8U, expected_data_b);
//     const auto ok = images_equal<unsigned char>(result_a, expected_a) && images_equal<unsigned char>(result_b, expected_b);
//     if (!ok)
//     {
//         tu::show_differences<unsigned char>(result_a, expected_a);
//         tu::show_differences<unsigned char>(result_b, expected_b);
//     }
//     CHECK(ok);
// }

TEST_CASE("Global Correlation", "[correlation]")
{
   CHECK(hkmn::global_correlation(1.0, 1.0) == 1);
   CHECK(hkmn::global_correlation(1.0, 0.0) == 0.5);
   CHECK(hkmn::global_correlation(0.0, 1.0) == 0.5);
   CHECK(hkmn::global_correlation(0.25, 0.3) == 0.3235294117647059);
}

TEST_CASE("XOR", "[XOR]")
{
    unsigned char data_1[] = {0, 0, 255, 255};
    unsigned char data_2[] = {0, 255, 0, 255,};
    unsigned char expected_data[] = {0, 255, 255, 0};
    const cv::Mat a = cv::Mat(1, 4, CV_8U, data_1);
    const cv::Mat b = cv::Mat(1, 4, CV_8U, data_2);
    const cv::Mat result = imp::x_or(a, b);
    const cv::Mat expected = cv::Mat(1, 4, CV_8U, expected_data);
    const auto ok = images_equal<unsigned char>(result, expected);
    if (!ok)
    {
        tu::show_differences<unsigned char>(result, expected);
    }
    CHECK(ok);
}

TEST_CASE("Convert Colour", "[hsv, bgr]")
{
    cv::Mat hsv = cv::Mat::zeros(1, 1, CV_8UC3);
    auto pixel = hsv.at<cv::Vec3b>(0, 0);
    pixel(0) = 0;
    pixel(1) = 255;
    pixel(2) = 255;
    const auto result = imp::convert_hsv_to_bgr(hsv);
    cv::Mat expected = cv::Mat::zeros(1, 1, CV_8UC3);
    auto pixel_2 = expected.at<cv::Vec3b>(0, 0);
    pixel_2(0) = 0;
    pixel_2(1) = 0;
    pixel_2(2) = 255;
    const auto ok = images_equal<unsigned char>(result, expected);
    if (!ok)
    {
        tu::show_differences<unsigned char>(result, expected);
    }
    CHECK(ok);
}


TEST_CASE("Hue Calculator", "[hsv, hue]")
{
    // This now has 1 based API as levels always start from 1
    const auto calculator = imp::HueCalculator::from(5, 10);
    CHECK(calculator.hue_per_level() == 2.0);
    CHECK(calculator.hue_for_level(1) == 0.0);
    CHECK(calculator.hue_for_level(2) == 2.0);
    CHECK(calculator.hue_for_level(3) == 4.0);
    CHECK(calculator.hue_for_level(4) == 6.0);
    CHECK(calculator.hue_for_level(5) == 8.0);
    CHECK(calculator.hue_for_level(6) == 10.0);
    CHECK(calculator.hue_for_level_t<std::uint8_t>(5) == 8);
}