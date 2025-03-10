package com.github.binarytojson.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing various data types used in a data transformation scenario. Each data type has a
 * primary value and a list of aliases that can be used to identify it.
 */
@SuppressWarnings("GrazieInspection")
@AllArgsConstructor
@Getter
public enum DataType {

    /** Represents a BIT data type. */
    BIT(Collections.singletonList("BIT\\((\\d+)\\)")),

    /** Represents a CHAR data type. */
    CHAR(Collections.singletonList("CHAR\\((\\d+)\\)")),

    /** Represents a FIXED data type. */
    FIXED(Arrays.asList("FIXED\\((\\d+),?(\\d*)\\)", "FIXED DEC\\((\\d+),?(\\d*)\\)")),

    /** Represents a FIXED_BINARY data type. */
    FIXED_BINARY(Arrays.asList("FIXED\\s+BIN\\((\\d+)\\)", "BINARY FIXED")),

    /** Represents a PIC data type. */
    PIC(Collections.singletonList("PIC'(\\d+)'"));

    /** List of aliases that can be used to identify the data type. */
    private final List<String> aliases;
}
