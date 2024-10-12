package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

/**
 * The BitReader class is responsible for reading bits from a list of bytes based on a specified PrimitiveType.
 */
public class BitReader implements TypeReader {
    private static final int MASK_FOR_DEFINE_BYTE = 0xFF;

    /**
     * Reads and extracts the binary value from a list of bytes based on the provided PrimitiveType.
     *
     * @param bytes the list of integers representing bytes to read from
     * @param type  the PrimitiveType specifying the start position and length of the bits to be read
     * @return a binary string representation of the extracted bits
     */
    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        int start = type.getStart();
        int numberOfBits = type.getNumberOfBits();
        StringBuilder builder = new StringBuilder();
        // Iterate through each byte in the list
        for (int i = 0; i < bytes.length; i++) {
            int currentByte = bytes[i] & MASK_FOR_DEFINE_BYTE;
            // Iterate through each bit in the byte from higher to lower
            for (int j = 7; j >= 0; j--) {
                // Extracting the j-th bit
                int bitValue = (currentByte >> j) & 1;
                // Calculate the global bit position
                int globalBitPosition = i * 8 + (7 - j);
                // Check if the bit is within the specified range
                if (start <= globalBitPosition && globalBitPosition < start + numberOfBits) {
                    builder.append(bitValue);
                }
                // Break the loop if we have collected enough bits
                if (builder.length() == numberOfBits) {
                    break;
                }
            }
        }
        return builder.toString();
    }
}
