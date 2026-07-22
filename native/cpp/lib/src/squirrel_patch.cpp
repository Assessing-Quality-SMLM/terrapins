#include "squirrel_patch.hpp"

#include <format>

namespace sqrl 
{
	Patch::Patch(int row_start, int row_end, int col_start, int col_end)
		: row_start_(row_start)
		, row_end_(row_end)
		, col_start_(col_start)
		, col_end_(col_end)
	{
	}

	Patch Patch::from(int row_start, int row_end, int col_start, int col_end)
	{
		return Patch(row_start, row_end, col_start, col_end);
	}

	int Patch::centre_row() const
	{
		return row_start_ + (width() / 2);
	}

	int Patch::centre_col() const
	{
		return col_start_ + (height() / 2);
	}

	cv::Rect Patch::to_rect() const
	{			
		return cv::Rect(col_start_, row_start_, width(), height());
	}

	bool Patch::is_equal(const Patch& other) const
	{
		// std::cout << std::format("{} vs {}\n", to_string(), other.to_string());
		return row_start_ == other.row_start_ &&
			   row_end_ == other.row_end_ &&
			   col_start_ == other.col_start_ &&
			   col_end_ == other.col_end_;
	}

	std::string Patch::to_string() const
	{
		return std::format("{},{},{},{}", row_start_, col_start_, row_end_, col_end_);
	}

	int Patch::width() const
	{
		return col_end_ - col_start_;
	}

	int Patch::height() const
	{
		return row_end_ - row_start_;
	}
}
