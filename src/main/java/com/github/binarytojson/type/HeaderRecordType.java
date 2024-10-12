package com.github.binarytojson.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing different types of header records in a data transformation scenario.
 */
@AllArgsConstructor
@Getter
public enum HeaderRecordType {

    /**
     * Represents a variable format header record.
     * Default value if absent - VB
     */
    VARIABLE_FORMAT("VB"),

    /**
     * Represents a fixed format header record.
     */
    FIXED_FORMAT("FB");

    /**
     * The primary value of the header record type.
     */
    private final String value;
}
