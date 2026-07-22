#include "results.hpp"

#include <filesystem>
#include <format>
#include <ios>
#include <iostream>

namespace hkmn
{
	ScoreStream::ScoreStream()
	{
	}

	bool ScoreStream::initialise_at(const std::string& output_directory)
	{
		auto filename = std::filesystem::path(output_directory) / "score";
		auto mode = std::ios_base::out | std::ios_base::app;
        stream_ = std::ofstream(filename, mode);
        return stream_.is_open();
	}

	bool ScoreStream::valid() const
	{
		return stream_.is_open() && check_stream_state();
	}

	bool ScoreStream::add_score(int level, double score)
	{
		std::lock_guard<std::mutex> guard(lock_);
		return write_to_stream(level, score);
	}

	bool ScoreStream::write_to_stream(int level, double score)
	{
        if (!stream_.is_open())
        {
            std::cout << std::format("stream is not open\n");
            return false;
        }
        try 
        {
            stream_ << std::format("{},{}\n", level, score);
            return check_stream_state();
        } 
        catch (const std::ios_base::failure& e) 
        {
            std::cout << e.what() << "\n";
            return false;
        }
	}

	bool ScoreStream::finalise()
	{
		stream_.close();
		return check_stream_state();
	}

	bool ScoreStream::check_stream_state() const
	{
		const auto state = stream_.rdstate();
        if (state & std::ios_base::goodbit)
            return true;
        if (state & std::ios_base::eofbit)
            std::cout << "Failure: stream at end of file\n";
        if (state & std::ios_base::failbit)
            std::cout << "Failure: encountered fail bit\n";
        if (state & std::ios_base::badbit)
            std::cout << "Failure: encountered bad bit\n";
        return false;
	}

	Results::Results()
	{

	}

	const std::vector<cv::Mat> Results::confidence_stack() const
	{
		auto stack = std::vector<cv::Mat>();
		for (const auto& [k, v]: confidence_stack_)
		{
			stack.push_back(v);
		}
		return stack;
	}

	bool Results::write_global_score(int level, double score)
	{
		return global_.add_score(level, score);
	}

	bool Results::write_sharpening(int level, double score)
	{
		return sharp_.add_score(level, score);
	}

	bool Results::write_structure(int level, double score)
	{
		return structure_.add_score(level, score);
	}

	bool Results::initialise_at(const std::string& output_directory)
	{
		const auto od = std::filesystem::path(output_directory);
		const auto cm = od / "confidence_map";
		const auto sharp = od / "sharpening_map";
		const auto structure = od / "structure_map";

		return (global_.initialise_at(cm.string())   && 
				sharp_.initialise_at(sharp.string()) && 
				structure_.initialise_at(structure.string()));
	}

	bool Results::add_confidence_map(int level, cv::Mat map)
	{
		// std::cout << "Waiting for lock\n";
		std::lock_guard<std::mutex> guard(lock_);
		// std::cout << "Adding to stack\n";
		confidence_stack_.insert({level, map});
		// std::cout << "leaving\n";
		return true;
	}

	bool Results::finalise()
	{
		return (global_.finalise() &&
				sharp_.finalise() &&
				structure_.finalise());
	}
}