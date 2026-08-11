#include "threads.hpp"

#include <thread>

namespace imp
{
	// right now its ok that we can take more threads
    // than levels. 
    // the fact that we only use n_levels worth
    // is an implementation detail that is not 
    // for settings to work out.
    unsigned int usable_threads(unsigned int specified_threads)
    {
        return std::min(specified_threads, system_threads());
    }

    unsigned int system_threads()
    {
        return std::thread::hardware_concurrency();
    }
}