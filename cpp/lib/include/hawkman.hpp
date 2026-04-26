#ifndef HAWKMAN_HPP_
#define HAWKMAN_HPP_

#include "cli.hpp" // keep this in so settings parsing is availble to api
#include "settings.hpp"


namespace hkmn 
{
	int run(const Settings& settings);
}
#endif //HAWKMAN_HPP_