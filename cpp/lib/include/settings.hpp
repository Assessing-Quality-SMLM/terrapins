#ifndef SETTINGS_HPP_
#define SETTINGS_HPP_

#include "threshold_settings.hpp"

#include <filesystem>
#include <string>

namespace hkmn 
{
	class Settings
	{
		using Path = std::filesystem::path;
	private:
		Settings();

	public:
		static Settings default_settings();

	public:
		const std::string& ref_image() const;
		void set_ref_image(std::string value);

		const std::string& test_image() const;
		void set_test_image(std::string value);

		std::uint8_t start_level() const;
		void set_start_level(std::uint8_t value);

		std::uint8_t end_level() const;
		
		std::uint8_t n_levels() const;
		void set_n_levels(std::uint8_t value);

		ThresholdSettings fwhm_threshold_settings() const;
		void set_fwhm_threshold_settings(ThresholdSettings settings);

		ThresholdSettings skeleton_threshold_settings() const;
		void set_skeleton_threshold_settings(ThresholdSettings settings);

		bool flatten_intensities() const;
		void set_flatten_intensities(bool value);

		double psf() const;
		void set_psf(double value);

		bool dilate_erode() const;
		void set_dilate_erode(bool value);

		// controls whether sharpening map
		// uses gaussian blurred skeletons
		bool blur_skeleton() const;
		void set_blur_skeleton(bool value);

		unsigned int usable_threads() const;
		unsigned int system_threads() const;
		std::uint8_t n_threads() const;
		void set_n_threads(std::uint8_t value);

		bool create_single_image_summary() const;
		double artifact_threshold() const;
		void set_artifact_threshold(double value);
		std::uint8_t consecutive_scales() const;
		void set_consecutive_scales(std::uint8_t value);

		const std::string& output_directory() const;
		void set_output_directory(std::string value);
		Path output_directory_path() const;
		Path confidence_map_dir() const;
		Path sharpening_map_dir() const;
		Path structure_map_dir() const;
		Path skeleton_map_dir() const;

	private:
		std::string ref_image_ = "";
		std::string test_image_ = "";
		int start_level_ = 1;
		int n_levels_ = 10;
		ThresholdSettings fwhm_threshold_settings_;
		ThresholdSettings skeleton_threshold_settings_;
		bool flatten_intensities_ = true;
		double psf_;
		bool dilate_erode_ = false;
		bool blur_skeletons_ = true;
		double artifact_threshold_ = 0.1;
		std::uint8_t consecutive_scales_ = 3;
		std::uint8_t n_threads_ = 0; // zero is single thread
		std::string output_directory_ = "./output";
	};
}
#endif //SETTINGS_HPP_