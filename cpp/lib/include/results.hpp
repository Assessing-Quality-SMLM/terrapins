#ifndef RESULTS_HPP_
#define RESULTS_HPP_

#include <opencv2/core/core.hpp>

#include <fstream>
#include <mutex>
#include <vector>

namespace hkmn 
{
    class ScoreStream
    {
    public:
        explicit ScoreStream();
        bool initialise_at(const std::string& output_directory);
        bool valid() const;
        bool add_score(int level, double score);
        bool finalise();
    private:
        bool write_to_stream(int level, double score);
        bool check_stream_state() const;
    private:
        std::mutex lock_;
        std::ofstream stream_;
    };

    class Results
    {
    public:
        explicit Results();
        const std::vector<cv::Mat> confidence_stack() const;

        bool initialise_at(const std::string& output_directory);
        bool write_global_score(int level, double score);
        bool write_sharpening(int level, double score);
        bool write_structure(int level, double score);
        bool add_confidence_map(cv::Mat image);
        bool finalise();
    private:
        std::mutex lock_;
        ScoreStream global_;  
        ScoreStream sharp_;  
        ScoreStream structure_;
        std::vector<cv::Mat> confidence_stack_;
    };
}
#endif //RESULTS_HPP_