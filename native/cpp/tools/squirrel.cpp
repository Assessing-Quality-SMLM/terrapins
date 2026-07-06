#include "squirrel.hpp"

#include <iostream>

int main(int argc, char const *argv[])
{
	auto settings = sqrl::get_settings(argc, argv);
	if (settings.wf_image() == "")
		return -1;
	if (settings.sr_image() == "")
		return -2;

	std::cout << "Widefield reference image: " << settings.wf_image() << "\n";
	std::cout << "Super res image: " << settings.sr_image() << "\n";
	return sqrl::run(settings);
}