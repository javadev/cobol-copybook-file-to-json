package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

/**
 * The PicReader class implements the TypeReader interface to read values from a list of bytes based
 * on a given PrimitiveType.
 */
public class PicReader implements TypeReader {

    private final EbcdicAsciiConvertor ebcdicAsciiConvertor;

    /** Constructs a PicReader with an instance of EbcdicAsciiConvertor. */
    public PicReader() {
        ebcdicAsciiConvertor = new EbcdicAsciiConvertor();
    }

    /**
     * Reads the value from the list of bytes based on the specified PrimitiveType.
     *
     * @param bytes the list of bytes to read from
     * @param type the PrimitiveType indicating the type of value to be read
     * @return a string representing the value read
     */
    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        return insertDotAtPosition(ebcdicAsciiConvertor.convert(bytes), type.getScaleFactor());
    }

    /**
     * Inserts a dot at the specified position from the end of the input string.
     *
     * @param input the input string to modify
     * @param positionFromEnd the position from the end where the dot should be inserted
     * @return the modified string with the dot inserted
     */
    String insertDotAtPosition(String input, int positionFromEnd) {
        // Check for invalid position
        if (positionFromEnd <= 0 || positionFromEnd > input.length()) {
            return input;
        }
        // Calculate the position from the beginning
        int positionFromStart = input.length() - positionFromEnd;
        // Insert the dot at the calculated position
        return input.substring(0, positionFromStart) + "." + input.substring(positionFromStart);
    }
}
