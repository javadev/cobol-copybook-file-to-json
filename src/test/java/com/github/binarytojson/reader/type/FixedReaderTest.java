package com.github.binarytojson.reader.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.binarytojson.type.PrimitiveType;

class FixedReaderTest {

    private FixedReader fixedReader;

    @BeforeEach
    void setUp() {
        fixedReader = new FixedReader();
    }

    @Test
    void testReadValuePositiveNumber() {
        // Representing a positive number
        byte[] bytes = {0x12, 0x34};
        // Digits: 4, Scale: 2
        PrimitiveType type = PrimitiveType.builder().digitsCount(4).scaleFactor(2).build();
        String result = fixedReader.readValue(bytes, type);
        assertEquals("12.34", result);
    }

    @Test
    void testReadValueNegativeNumber() {
        // Representing a negative number (-12.3)
        byte[] bytes = {0x12, (byte) 0x3D};
        // Digits: 3, Scale: 1
        PrimitiveType type = PrimitiveType.builder().digitsCount(3).scaleFactor(1).build();
        String result = fixedReader.readValue(bytes, type);
        assertEquals("-12.3", result);
    }

    @Test
    void testReadValueNoDecimal() {
        // Representing a whole number (5678)
        byte[] bytes = {0x56, 0x78};
        // Digits: 4, Scale: 0
        PrimitiveType type = PrimitiveType.builder().digitsCount(4).scaleFactor(0).build();
        String result = fixedReader.readValue(bytes, type);
        assertEquals("5678", result);
    }

    @Test
    void testReadValueZeroBytes() {
        // Empty byte array
        byte[] bytes = {};
        // Digits and scale both 0
        PrimitiveType type = PrimitiveType.builder().digitsCount(0).scaleFactor(0).build();
        String result = fixedReader.readValue(bytes, type);
        assertEquals("", result);
    }

    @Test
    void testReadValueSingleDigitWithScale() {
        // Representing 0.5
        byte[] bytes = {0x05};
        PrimitiveType type = PrimitiveType.builder()
                .digitsCount(2).scaleFactor(1).build();
        String result = fixedReader.readValue(bytes, type);
        assertEquals("0.5", result);
    }
}
