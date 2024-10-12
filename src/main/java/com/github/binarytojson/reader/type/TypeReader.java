package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

/**
 * Interface for reading values from a list of bytes based on a specified data type.
 */
public interface TypeReader {

    /**
     * Reads a value from a list of bytes based on the specified data type.
     *
     * @param bytes list of integers representing bytes/halfBytes from which a value is read
     * @param type  the PrimitiveType representing the data type for reading the value
     * @return string representing the value read from the bytes based on the specified data type
     */
    String readValue(byte[] bytes, PrimitiveType type);
}
