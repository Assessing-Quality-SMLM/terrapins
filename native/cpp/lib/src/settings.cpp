#include "settings.hpp"

#include <thread>

namespace hkmn 
{
    ThresholdSettings::ThresholdSettings()
    {

    }

    ThresholdSettings ThresholdSettings::default_settings()
    {
        return ThresholdSettings();
    }

    double ThresholdSettings::threshold() const
    {
        return threshold_;
    }

    void ThresholdSettings::set_threshold(double value)
    {
        threshold_ = value;
    }

    double ThresholdSettings::smoothing() const
    {
        return smooth_;
    }

    void ThresholdSettings::set_smoothing(double value)
    {
        smooth_ = value;
    }

    double ThresholdSettings::offset() const
    {
        return offset_;
    }

    void ThresholdSettings::set_offset(double value)
    {
        offset_ = value;
    }

    Settings::Settings()
        : fwhm_threshold_settings_(ThresholdSettings::default_settings())
        , skeleton_threshold_settings_(ThresholdSettings::default_settings())
    {
    };

    Settings Settings::default_settings()
    {
        auto settings = Settings();
        settings.set_n_levels(10);

        settings.fwhm_threshold_settings_.set_threshold(0.7);
        settings.fwhm_threshold_settings_.set_smoothing(0.1);
        settings.fwhm_threshold_settings_.set_offset(0.04);

        settings.skeleton_threshold_settings_.set_threshold(0.85);
        settings.skeleton_threshold_settings_.set_smoothing(0.1);
        settings.skeleton_threshold_settings_.set_offset(0.02);

        settings.set_flatten_intensities(false);
        settings.set_psf(13.2);
        settings.set_blur_skeleton(true);

        settings.set_artifact_threshold(0.1);
        settings.set_consecutive_scales(3);

        settings.set_n_threads(20);

        settings.set_output_directory("./output");
        return settings;
    }

    const std::string& Settings::ref_image() const
    {
        return ref_image_;
    }

    void Settings::set_ref_image(std::string value)
    {
        ref_image_ = value;
    }

    const std::string& Settings::test_image() const
    {
        return test_image_;
    }

    void Settings::set_test_image(std::string value)
    {
        test_image_ = value;
    }

    std::uint8_t Settings::start_level() const
    {
        return start_level_;
    }

    void Settings::set_start_level(std::uint8_t value)
    {
        start_level_ = value;
    }

    std::uint8_t Settings::n_levels() const
    {
        return n_levels_;
    }
    
    void Settings::set_n_levels(std::uint8_t value)
    {
        n_levels_ = value;
    }

    std::uint8_t Settings::end_level() const
    {
        return start_level() + n_levels();
    }
    
    ThresholdSettings Settings::fwhm_threshold_settings() const
    {
        return fwhm_threshold_settings_;
    }

    void Settings::set_fwhm_threshold_settings(ThresholdSettings settings)
    {
        fwhm_threshold_settings_ = settings;
    }

    ThresholdSettings Settings::skeleton_threshold_settings() const
    {
        return skeleton_threshold_settings_;
    }

    void Settings::set_skeleton_threshold_settings(ThresholdSettings settings)
    {
        skeleton_threshold_settings_ = settings;
    }

    bool Settings::flatten_intensities() const
    {
        return flatten_intensities_;
    }

    void Settings::set_flatten_intensities(bool value)
    {
        flatten_intensities_ = value;
    }

    double Settings::psf() const
    {
        return psf_;
    }

    void Settings::set_psf(double value)
    {
        psf_ = value;
    }

    bool Settings::dilate_erode() const
    {
        return dilate_erode_;
    }

    void Settings::set_dilate_erode(bool value)
    {
        dilate_erode_ = value;
    }

    bool Settings::blur_skeleton() const
    {
    	return blur_skeletons_;
    }

    void Settings::set_blur_skeleton(bool value)
    {
    	blur_skeletons_ = value;
    }

    std::uint8_t Settings::n_threads() const
    {
        return n_threads_;
    }

    // right now its ok that we can take more threads
    // than levels. 
    // the fact that we only use n_levels worth
    // is an implementation detail that is not 
    // for settings to work out.
    unsigned int Settings::usable_threads() const
    {
        auto n = static_cast<unsigned int>(n_threads());
        if (n == 0) // unset do what we want
            n = static_cast<unsigned int>(n_levels());
        return std::min(n, system_threads());
    }

    unsigned int Settings::system_threads() const
    {
        return std::thread::hardware_concurrency();
    }

    void Settings::set_n_threads(std::uint8_t value)
    {
        n_threads_ = value;
    }

    bool Settings::create_single_image_summary() const
    {
        return start_level() == 1;
    }

    double Settings::score_threshold() const
    {
        return score_threshold_;
    }

    void Settings::set_score_threshold(double value)
    {
        score_threshold_ = value;
    } 

    double Settings::artifact_threshold() const
    {
        return artifact_threshold_;
    }

    void Settings::set_artifact_threshold(double value)
    {
        artifact_threshold_ = value;
    }

    std::uint8_t Settings::consecutive_scales() const
    {
        return consecutive_scales_;
    }

    void Settings::set_consecutive_scales(std::uint8_t value)
    {
        consecutive_scales_ = value;
    } 

    const std::string& Settings::output_directory() const
    {
        return output_directory_;
    }

    void Settings::set_output_directory(std::string value)
    {
        output_directory_ = value;
    }

    Settings::Path Settings::output_directory_path() const
    {
    	return Settings::Path(output_directory());
    }

    Settings::Path Settings::confidence_map_dir() const
    {
    	return output_directory_path() / "confidence_map";
    }

    Settings::Path Settings::sharpening_map_dir() const
    {
    	return output_directory_path() / "sharpening_map";
    }

    Settings::Path Settings::structure_map_dir() const
    {
    	return output_directory_path() / "structure_map";
    }

    Settings::Path Settings::skeleton_map_dir() const
    {
        return output_directory_path() / "skeleton_map";
    }
}