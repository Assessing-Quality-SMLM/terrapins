#include "squirrel_settings.hpp"

namespace sqrl
{
	Settings::Settings()
	{

	}

	Settings Settings::default_settings()
	{
		return Settings();
	}

	const std::string& Settings::wf_image() const
	{
		return wf_image_;
	}

	void Settings::set_wf_image(std::string value)
	{
		wf_image_ = value;
	}

	const std::string& Settings::sr_image() const
	{
		return sr_image_;
	}

	void Settings::set_sr_image(std::string value)
	{
		sr_image_ = value;
	}

	Settings::Path Settings::output_directory() const
	{
		const auto core = Settings::Path(output_directory_);
        return std::filesystem::weakly_canonical(core);
	}

	void Settings::set_output_directory(std::string value)
	{
		output_directory_ = value;
	}

	double Settings::sigma_nm() const
	{
		return sigma_nm_;
	}

	void Settings::set_sigma_nm(double value)
	{
		sigma_nm_ = value;
	}

	double Settings::pixel_size_nm() const
	{
		return pixel_size_nm_;
	}

	void Settings::set_pixel_size_nm(double value)
	{
		pixel_size_nm_ = value;
	}

	bool Settings::crop_borders() const
	{
		return crop_borders_;
	}

	void Settings::set_crop_borders(bool value)
	{
		crop_borders_ = value;
	}

	bool Settings::perform_registration() const
	{
		return register_;
	}

	void Settings::set_registration(bool value)
	{
		register_ = value;
	}

	bool Settings::show_positive_negative() const
	{
		return show_positive_negative_;
	}

	void Settings::set_show_positive_negative(bool value)
	{
		show_positive_negative_ = value;
	}

	bool Settings::write_optimiser_data() const
	{
		return write_optimiser_;
	}

	void Settings::set_write_optimiser_data(bool value)
	{
		write_optimiser_ = value;
	}

	bool Settings::patchwise() const
	{
		return patchwise_;
	}

	void Settings::set_patchwise(bool value)
	{
		patchwise_ = value;
	}

	int Settings::patch_size() const
	{
		return sr_patch_size_;
	}

	void Settings::set_patch_size(int value)
	{
		sr_patch_size_ = value;
	}

	int Settings::step_size() const
	{
		return sr_step_size_;
	}

	void Settings::set_step_size(int value)
	{
		sr_step_size_ = value;
	}

	bool Settings::use_mt() const
	{
		return use_mt_;
	}

	void Settings::set_mt(bool value)
	{
		use_mt_ = value;
	}

	std::uint8_t Settings::n_threads() const
	{
		return n_threads_;
	}

	void Settings::set_n_threads(std::uint8_t value)
	{
		n_threads_ = value;
	}
}