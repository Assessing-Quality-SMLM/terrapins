#ifndef SQUIRREL_SETTINGS_HPP_
#define SQUIRREL_SETTINGS_HPP_

#include <filesystem>
#include <optional>
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
		bool display_algorithms() const;
		void set_display_algorithms(bool value);

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

		int wf_border_size() const;
		void set_wf_border_size(int value);

		bool perform_registration() const;
		void set_registration(bool value);

		std::string registration_method() const;
		//https://diplib.org/diplib-docs/analysis.html#dip-FindShift-Image-CL-Image-CL-String-CL-dfloat--UnsignedArray-
		void set_registration_method(std::string value);

		bool show_positive_negative() const;
		void set_show_positive_negative(bool value);

		std::optional<std::string> optimiser_algorithm() const;
		void set_optimiser_algorithm(std::string value);

		bool write_optimiser_data() const;
		void set_write_optimiser_data(bool value);

		bool three_parameter_solve() const;
		void set_three_parameter_solve(bool value);

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
		bool display_algorithms_ = false;

		std::string output_directory_ = ".";
		std::string wf_image_ = "";
		std::string sr_image_ = "";

		double pixel_size_nm_ = 100.0;
		double sigma_nm_ = 200.0;
		int crop_borders_ = 2;
		bool register_ = true;
		std::optional<std::string> registration_method_ = {};
		bool show_positive_negative_ = true;
		std::optional<std::string> optimiser_algorithm_ = {};
		bool write_optimiser_ = false;
		bool three_parameter_solve_ = false;
		bool patchwise_ = true;
		int sr_patch_size_ = 32;
		int sr_step_size_ = 16;
		bool use_mt_ = true;
		std::uint8_t n_threads_ = 4;
	};
}
#endif //SQUIRREL_SETTINGS_HPP_