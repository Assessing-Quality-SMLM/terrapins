#include "squirrel.hpp"

#include <iostream>

int main(int argc, char const *argv[])
{
	std::string wf_file = "/home/nik/Documents/support/squirrel/reference.tif";
	std::string sr_file = "/home/nik/Documents/support/squirrel/super-resolution.tif";

	auto settings = sqrl::get_settings(argc, argv);
	if (settings.wf_image() == "")
		settings.set_wf_image(wf_file);
	if (settings.sr_image() == "")
		settings.set_sr_image(sr_file);

	std::cout << "Widefield reference image: " << settings.wf_image() << "\n";
	std::cout << "Super res image: " << settings.sr_image() << "\n";
	return sqrl::run(settings);
}