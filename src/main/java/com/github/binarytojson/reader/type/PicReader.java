package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

/**
 * The PicReader class implements the TypeReader interface to read values from a list of bytes based on a given PrimitiveType.
 */
public class PicReader implements TypeReader {

    private final EbcdicAsciiConvertor ebcdicAsciiConvertor;

    /**
     * Constructs a PicReader with an instance of EbcdicAsciiConvertor.
     */
    public PicReader() {
        ebcdicAsciiConvertor = new EbcdicAsciiConvertor();
    }

    /**
     * Reads the value from the list of bytes based on the specified PrimitiveType.
     *
     * @param bytes the list of bytes to read from
     * @param type  the PrimitiveType indicating the type of value to be read
     * @return a string representing the value read
     */
    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        return ebcdicAsciiConvertor.convert(bytes);
    }
}
