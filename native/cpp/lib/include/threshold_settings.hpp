#ifndef THRESHOLD_SETTINGS_HPP_
#define THRESHOLD_SETTINGS_HPP_
namespace hkmn 
{
	class ThresholdSettings
	{
	private:
		ThresholdSettings();

	public:
		static ThresholdSettings default_settings();
		double threshold() const;
		void set_threshold(double value);
		double smoothing() const;
		void set_smoothing(double value);
		double offset() const;
		void set_offset(double value);

	private:
		double threshold_;
		double smooth_;
		double offset_;
	};
}
#endif //THRESHOLD_SETTINGS_HPP_