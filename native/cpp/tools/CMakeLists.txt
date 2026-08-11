cmake_minimum_required(VERSION 3.10.0)
project("Tools" VERSION 0.1.0 LANGUAGES CXX)

set(HAWKMAN_LIB_DIR "../lib")

add_subdirectory(${HAWKMAN_LIB_DIR} "./lib_build")

set(HAWKMAN_SOURCE	"./hawkman.cpp")
add_executable("hawkman" ${HAWKMAN_SOURCE})
target_include_directories("hawkman" PRIVATE "../lib/include")
target_link_libraries("hawkman" PRIVATE "hawkman_lib")
target_compile_features("hawkman" PRIVATE cxx_std_23)
set_target_properties("hawkman" PROPERTIES CXX_EXTENSIONS OFF)

set(SQUIRREL_SOURCE	"./squirrel.cpp")
add_executable("squirrel" ${SQUIRREL_SOURCE})
target_include_directories("squirrel" PRIVATE "../lib/include")
target_link_libraries("squirrel" PRIVATE "hawkman_lib")
target_compile_features("squirrel" PRIVATE cxx_std_23)
set_target_properties("squirrel" PROPERTIES CXX_EXTENSIONS OFF)
