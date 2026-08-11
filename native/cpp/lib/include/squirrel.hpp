#ifndef SQUIRREL_HPP_
#define SQUIRREL_HPP_

#include "cli.hpp" // keep this in so settings parsing is availble to api
#include "squirrel_settings.hpp"


namespace sqrl 
{
	int run(const Settings& settings);
}
#endif //SQUIRREL_HPP_