cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections -fvisibility=hidden")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffunction-sections -fdata-sections -fvisibility=hidden")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections")

##########################################################
set(freetype2_path ${CMAKE_SOURCE_DIR}/../../../../freetype2)
set(freetype2_include ${freetype2_path}/include)
set(freetype2_a_path ${freetype2_path}/alibs/${ANDROID_ABI})
include_directories(${freetype2_include})

##########################################################
set(freetype2_a ${freetype2_a_path}/libfreetype.a)
add_library(freetype2lib STATIC IMPORTED)
set_target_properties(freetype2lib PROPERTIES
        IMPORTED_LINK_INTERFACE_LANGUAGES "C"
        IMPORTED_LINK_INTERFACE_LIBRARIES "z"
        IMPORTED_LOCATION ${freetype2_a}
        )

##########################################################
set(assimp_path ${CMAKE_SOURCE_DIR}/../../../../assimp)
set(assimp_include ${assimp_path}/include)
set(assimp_a_path ${assimp_path}/alibs/${ANDROID_ABI})
include_directories(${assimp_include})

##########################################################
set(assimp_a ${assimp_a_path}/libassimp.a)
add_library(assimplib STATIC IMPORTED)
set_target_properties(assimplib PROPERTIES
        IMPORTED_LINK_INTERFACE_LANGUAGES "CXX"
        IMPORTED_LINK_INTERFACE_LIBRARIES "z;m;atomic"
        IMPORTED_LOCATION ${assimp_a}
)

##########################################################
include_directories(./)

include_directories(./image)
aux_source_directory(./image image)

include_directories(./java)
aux_source_directory(./java java)

include_directories(./glm)

include_directories(./example1)
aux_source_directory(./example1 example1)

include_directories(./example2)
aux_source_directory(./example2 example2)

include_directories(./example3)
aux_source_directory(./example3 example3)

include_directories(./example4)
aux_source_directory(./example4 example4)

include_directories(./example5)
aux_source_directory(./example5 example5)

include_directories(./example6)
aux_source_directory(./example6 example6)

include_directories(./example7)
aux_source_directory(./example7 example7)

include_directories(./example8)
aux_source_directory(./example8 example8)

include_directories(./example9)
aux_source_directory(./example9 example9)

include_directories(./example10)
aux_source_directory(./example10 example10)

include_directories(./example11)
aux_source_directory(./example11 example11)

include_directories(./example13)
aux_source_directory(./example13 example13)

include_directories(./example14)
aux_source_directory(./example14 example14)

include_directories(./example15)
aux_source_directory(./example15 example15)

include_directories(./example16 ./example16/filter)
aux_source_directory(./example16 example16)
aux_source_directory(./example16/filter example16_filter)

include_directories(./example17)
aux_source_directory(./example17 example17)

include_directories(./example19)
aux_source_directory(./example19 example19)

add_library(
        native-lib

        SHARED

        ${image}
        ${java}
        base_Log.cpp
        esUtils.cpp
        ${example1}
        ${example2}
        ${example3}
        ${example4}
        ${example5}
        ${example6}
        ${example7}
        ${example8}
        ${example9}
        ${example10}
        ${example11}
        ${example13}
        ${example14}
        ${example15}
        ${example16}
        ${example16_filter}
        ${example17}
        ${example19}
        native-lib.cpp)

find_library(
        log-lib

        log)

target_link_libraries(
        native-lib

        android
        jnigraphics
        GLESv3
        m
        #EGL
        freetype2lib
        optimized
        atomic
        z
        assimplib
        ${log-lib})