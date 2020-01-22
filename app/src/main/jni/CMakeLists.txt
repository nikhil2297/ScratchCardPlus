cmake_minimum_required(VERSION 3.6)

add_library(bitmap-processing SHARED ${PROJECT_SOURCE_DIR}/bitmap-processing.cpp)
target_link_libraries(bitmap-processing jnigraphics android log)