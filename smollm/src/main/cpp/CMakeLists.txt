cmake_minimum_required(VERSION 3.22.1)

project("smollm")

set(LLAMA_DIR "../../../../llama.cpp")
set(GGML_DIR ${LLAMA_DIR}/ggml)
set(COMMON_DIR ${LLAMA_DIR}/common)

add_library(${CMAKE_PROJECT_NAME} SHARED
        smollm.cpp
        LLMInference.cpp
        GGUFReader.cpp
        ${LLAMA_DIR}/src/llama.cpp
        ${GGML_DIR}/src/ggml.c
        ${GGML_DIR}/src/ggml-alloc.c
        ${GGML_DIR}/src/ggml-backend.c
        ${GGML_DIR}/src/ggml-quants.c
        ${COMMON_DIR}/common.cpp
        ${COMMON_DIR}/console.cpp
        ${COMMON_DIR}/sampling.cpp
        ${COMMON_DIR}/grammar-parser.cpp
        ${COMMON_DIR}/json-schema-to-grammar.cpp
        ${COMMON_DIR}/train.cpp
        ${COMMON_DIR}/ngram-cache.cpp
        ${COMMON_DIR}/speculative.cpp
        ${COMMON_DIR}/stb_image.h
        )

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        ${LLAMA_DIR}/
        ${LLAMA_DIR}/src/
        ${GGML_DIR}/
        ${GGML_DIR}/include/
        ${GGML_DIR}/src/
        ${COMMON_DIR}/
        )

target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
        GGML_USE_LLAMAFILE=1
        NDEBUG=1
        )

find_library(log-lib log)

target_link_libraries(${CMAKE_PROJECT_NAME} ${log-lib})