cmake_minimum_required(VERSION 3.10.0)
project("HAWKMAN" VERSION 0.1.0 LANGUAGES CXX)

find_package(OpenCV REQUIRED core imgcodecs)
# set(NLopt_DIR "C:/Users/k1651658/Documents/Repositories/nlopt/nlopt-2.10.0/build")
# set(NLopt_DIR "/home/nik/Documents/repositories/nlopt/nlopt-2.10.0/build")
find_package("NLopt")

# add_subdirectory(${DLIB_DIR} "./dlib_build")

# set(DIPLIB_DIR "/home/nik/Documents/repositories/diplib/diplib-3.5.2")
add_subdirectory(${DIPLIB_DIR} "./diplib_build")

set(DIP_BUILD_JAVAIO OFF PARENT_SCOPE)
set(DIP_BUILD_DIPIMAGE OFF PARENT_SCOPE)
set(DIP_BUILD_DIPVIEWER OFF PARENT_SCOPE)
set(DIP_BUILD_PYDIP OFF PARENT_SCOPE)
set(DIP_SHARED_LIBRARY OFF PARENT_SCOPE)
set(DIP_ENABLE_DOCTEST OFF PARENT_SCOPE)

set(HAWKMAN_LIB "hawkman_lib")
set(SOURCE	"./src/cli.cpp"
			"./src/image.cpp"
			"./src/imp.cpp"
			"./src/threads.cpp"
			"./src/hawkman.cpp"
			"./src/hue_calculator.cpp"
			"./src/resizing.cpp"
			"./src/results.cpp"
			"./src/settings.cpp"
			"./src/squirrel.cpp"
			"./src/squirrel_patch.cpp"
			"./src/squirrel_pw_results.cpp"
			"./src/squirrel_settings.cpp"
			)

add_library(${HAWKMAN_LIB} STATIC ${SOURCE})
target_include_directories(${HAWKMAN_LIB} PUBLIC "./include" ${OpenCV_INCLUDE_DIRS} ${NLOPT_INCLUDE_DIRS})
target_link_libraries(${HAWKMAN_LIB} PUBLIC "DIP" PRIVATE ${NLOPT_LIBRARIES} ${OpenCV_LIBS})
target_compile_features(${HAWKMAN_LIB} PRIVATE cxx_std_23)
set_target_properties(${HAWKMAN_LIB} PROPERTIES CXX_EXTENSIONS OFF)
