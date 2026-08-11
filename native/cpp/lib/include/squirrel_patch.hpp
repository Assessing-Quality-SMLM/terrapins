#ifndef SQUIRREL_PATCH_HPP_
#define SQUIRREL_PATCH_HPP_

#include <opencv2/core.hpp>

namespace sqrl 
{
    class Patch
	{
	private:
		explicit Patch(int row_start, int row_end, int col_start, int col_end);

	public:
		static Patch from(int row_start, int row_end, int col_start, int col_end);

		int centre_row() const;
		int centre_col() const;

		cv::Rect to_rect() const;

		bool is_equal(const Patch& other) const;

		std::string to_string() const;

	private:
		int width() const;
		int height() const;
	private:
		int row_start_;
		int row_end_;
		int col_start_;
		int col_end_;
	};
}
#endif //SQUIRREL_PATCH_HPP_