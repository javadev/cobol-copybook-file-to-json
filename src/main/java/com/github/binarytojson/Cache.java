package com.github.binarytojson;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumeration representing different cache sizes. */
@AllArgsConstructor
@Getter
public enum Cache {

    /** Default cache size of 8192 bytes. */
    DEFAULT(8192),

    /** Cache size of ten megabytes (1024 * 1024 * 10 bytes). */
    TEN_MEGABYTES(1024 * 1024 * 10);

    private final int bufferSize;
}
