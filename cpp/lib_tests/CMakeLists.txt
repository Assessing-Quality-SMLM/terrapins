cmake_minimum_required(VERSION 3.10.0)
project("Tests" VERSION 0.1.0 LANGUAGES CXX)

find_package(OpenCV REQUIRED)

# set("Catch2_DIR" "/home/nik/Documents/repositories/catch2/Catch2-3.8.1")
add_subdirectory(${Catch2_DIR} "./catch2_build")

set(HAWKMAN_LIB_DIR "../lib")
add_subdirectory(${HAWKMAN_LIB_DIR} "./lib_build")

set(DIP_BUILD_JAVAIO OFF PARENT_SCOPE)
set(DIP_BUILD_DIPIMAGE OFF PARENT_SCOPE)
set(DIP_BUILD_DIPVIEWER OFF PARENT_SCOPE)
set(DIP_BUILD_PYDIP OFF PARENT_SCOPE)
set(DIP_SHARED_LIBRARY OFF PARENT_SCOPE)
set(DIP_ENABLE_DOCTEST OFF PARENT_SCOPE)

set(HAWKMAN_TESTS "htests")
add_executable(${HAWKMAN_TESTS} tests.cpp 
					 		    cli_tests.cpp
					 		    histogram_tests.cpp
					 		    utils.cpp
					 		    settings_tests.cpp)
target_include_directories(${HAWKMAN_TESTS} PRIVATE "." "${HAWKMAN_LIB_DIR}/include" ${OpenCV_INCLUDE_DIRS})
target_link_libraries(${HAWKMAN_TESTS} PRIVATE "hawkman_lib" ${OpenCV_LIBS} Catch2::Catch2WithMain)
target_compile_features(${HAWKMAN_TESTS} PRIVATE cxx_std_23)
set_target_properties(${HAWKMAN_TESTS} PROPERTIES CXX_EXTENSIONS OFF)

set(SQUIRREL_TESTS "stests")
add_executable(${SQUIRREL_TESTS} squirrel_tests.cpp utils.cpp)
target_include_directories(${SQUIRREL_TESTS} PRIVATE "." "${HAWKMAN_LIB_DIR}/include" ${OpenCV_INCLUDE_DIRS})
target_link_libraries(${SQUIRREL_TESTS} PRIVATE "hawkman_lib" ${OpenCV_LIBS} Catch2::Catch2WithMain)
target_compile_features(${SQUIRREL_TESTS} PRIVATE cxx_std_23)
set_target_properties(${SQUIRREL_TESTS} PROPERTIES CXX_EXTENSIONS OFF)

# IF (WIN32)
# 	target_compile_definitions(tests PRIVATE WINDOWS=1 NIX=0)
# ELSE()
# 	target_compile_definitions(tests PRIVATE WINDOWS=0 NIX=1)
# ENDIF()
# include(CTest)
# include(Catch)
# catch_discover_tests(tests)