#ifndef SQUIRREL_SETTINGS_HPP_
#define SQUIRREL_SETTINGS_HPP_

#include <filesystem>
#include <string>

namespace sqrl 
{
	class Settings
	{
		using Path = std::filesystem::path;
	private:
		Settings();

	public:
		static Settings default_settings();

	public:
		const std::string& wf_image() const;
		void set_wf_image(std::string value);

		const std::string& sr_image() const;
		void set_sr_image(std::string value);

		Path output_directory() const;
		void set_output_directory(std::string value);

		double pixel_size_nm() const;
		void set_pixel_size_nm(double value);

		double sigma_nm() const;
		void set_sigma_nm(double value);

		bool crop_borders() const;
		void set_crop_borders(bool value);

		bool perform_registration() const;
		void set_registration(bool value);

		bool show_positive_negative() const;
		void set_show_positive_negative(bool value);

		bool write_optimiser_data() const;
		void set_write_optimiser_data(bool value);

		bool patchwise() const;
		void set_patchwise(bool value);

		int patch_size() const;
		void set_patch_size(int value);

		int step_size() const;
		void set_step_size(int value);

		std::uint8_t n_threads() const;
		void set_n_threads(std::uint8_t value);

		bool use_mt() const;
		void set_mt(bool value);

	private:
		std::string output_directory_ = ".";
		std::string wf_image_ = "";
		std::string sr_image_ = "";

		double pixel_size_nm_ = 100.0;
		double sigma_nm_ = 200.0;
		bool crop_borders_ = true;
		bool register_ = true;
		bool show_positive_negative_ = true;
		bool write_optimiser_ = false;
		bool patchwise_ = true;
		int sr_patch_size_ = 32;
		int sr_step_size_ = 16;
		bool use_mt_ = true;
		std::uint8_t n_threads_ = 4;
	};
}
#endif //SQUIRREL_SETTINGS_HPP_