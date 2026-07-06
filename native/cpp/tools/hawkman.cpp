#include "hawkman.hpp"

#include <iostream>

int main(int argc, char const *argv[])
{
	// std::string ref_file = argv[1];
	std::string ref_file = "/home/nik/Documents/support/hawkman/LMC-MT-ME-HAWK16.tif";
	// std::string test_file = argv[2];
	std::string test_file = "/home/nik/Documents/support/hawkman/LMC-MT-ME-Raw16.tif";

	auto settings = hkmn::get_settings(argc, argv);
	if (settings.ref_image() == "")
		settings.set_ref_image(ref_file);
	if (settings.test_image() == "")
		settings.set_test_image(test_file);

	std::cout << "Reference image (HAWK): " << settings.ref_image() << "\n";
	std::cout << "Test image (no HAWK): " << settings.test_image() << "\n";
	hkmn::run(settings);
	return 0;
}