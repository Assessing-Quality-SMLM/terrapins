#ifndef THREADS_HPP_
#define THREADS_HPP_

namespace imp
{
    unsigned int usable_threads(unsigned int specified_threads);
    unsigned int system_threads();
}
#endif //THREADS_HPP_