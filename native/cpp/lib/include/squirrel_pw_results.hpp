#ifndef SQUIRREL_PW_RESULTS_HPP_
#define SQUIRREL_PW_RESULTS_HPP_

#include "squirrel_parameters.hpp"

#include <opencv2/core.hpp>

#include <filesystem>

namespace sqrl 
{
	class PWResults
	{
	private:
		explicit PWResults(cv::Size map_size);
	public:
		static PWResults from(cv::Size map_size);
		void add(int row, int col, const Parameters& parameters);
		void write_to(const std::filesystem::path& output_directory);
	private:
		cv::Mat alpha_map_;
		cv::Mat beta_map_;
		cv::Mat sigma_map_;
		std::mutex mutex_;
	};
}
#endif //SQUIRREL_PW_RESULTS_HPP_