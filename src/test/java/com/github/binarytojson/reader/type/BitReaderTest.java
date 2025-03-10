package com.github.binarytojson.reader.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.binarytojson.type.PrimitiveType;
import org.junit.jupiter.api.Test;

class BitReaderTest {

    private final BitReader bitReader = new BitReader();

    @Test
    void testReadValueSingleByte() {
        // Binary representation: 10101010
        byte[] bytes = {(byte) 0b10101010};
        // Read first 4 bits
        PrimitiveType type = PrimitiveType.builder().start(0).numberOfBits(4).build();
        // Expected result
        String expected = "1010";
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to read 4 bits from a single byte");
    }

    @Test
    void testReadValueMultipleBytes() {
        // Binary representation: 10101010 11001100
        byte[] bytes = {(byte) 0b10101010, (byte) 0b11001100};
        // Read first 8 bits
        PrimitiveType type = PrimitiveType.builder().start(0).numberOfBits(8).build();
        String expected = "10101010"; // Expected result
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to read 8 bits from multiple bytes");
    }

    @Test
    void testReadValuePartialByte() {
        // Binary representation: 10101010
        byte[] bytes = {(byte) 0b10101010};
        // Read bits from position 4
        PrimitiveType type = PrimitiveType.builder().start(4).numberOfBits(4).build();
        String expected = "1010"; // Expected result
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to read 4 bits from a partial byte");
    }

    @Test
    void testReadValueBeyondByteLength() {
        // Binary representation: 10101010
        byte[] bytes = {(byte) 0b10101010};
        // Requesting more bits than available
        PrimitiveType type = PrimitiveType.builder().start(4).numberOfBits(8).build();
        // Expected result: we fill with zeros
        String expected = "1010";
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to handle request for more bits than available");
    }

    @Test
    void testReadValueNoBits() {
        // Binary representation: 10101010
        byte[] bytes = {(byte) 0b10101010};
        // Read 0 bits
        PrimitiveType type = PrimitiveType.builder().start(0).numberOfBits(0).build();
        // Expected result
        String expected = "";
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to handle reading 0 bits");
    }

    @Test
    void testReadValueInsufficientBits() {
        // Binary representation: 10101010
        byte[] bytes = {(byte) 0b10101010};
        // Requesting more bits than available
        PrimitiveType type = PrimitiveType.builder().start(0).numberOfBits(10).build();
        // Expected result: reading available bits, filling with zeros
        String expected = "10101010";
        String actual = bitReader.readValue(bytes, type);
        assertEquals(expected, actual, "Failed to handle insufficient bits scenario");
    }
}
