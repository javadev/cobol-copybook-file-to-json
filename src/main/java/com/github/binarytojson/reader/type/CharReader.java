package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

/**
 * Implementation of TypeReader for reading values of CHAR type.
 */
public class CharReader implements TypeReader {

    private final EbcdicAsciiConvertor ebcdicAsciiConvertor;

    /**
     * Constructs a CharWriter with an instance of EbcdicAsciiConvertor.
     */
    public CharReader() {
        ebcdicAsciiConvertor = new EbcdicAsciiConvertor();
    }

    /**
     * Reads the value from the given list of bytes for CHAR type.
     * The CHAR value is obtained by converting the EBCDIC bytes to ASCII using a conversion table.
     *
     * @param bytes list of integers representing bytes from which to read the value
     * @param type  the PrimitiveType representing the data type (CHAR in this case)
     * @return string representing the value read from the bytes for CHAR type
     */
    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        return ebcdicAsciiConvertor.convert(bytes);
    }
}
