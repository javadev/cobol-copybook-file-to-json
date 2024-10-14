package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the PicReader class.
 */
class PicReaderTest {

    private static final String NUMBER = "123456";
    private PicReader picReader;
    private EbcdicAsciiConvertor ebcdicAsciiConvertor;

    @BeforeEach
    void setUp() {
        // Mocking EbcdicAsciiConvertor to control its behavior in tests
        ebcdicAsciiConvertor = mock(EbcdicAsciiConvertor.class);
        picReader = new PicReader();
    }

    @Test
    void testReadValueWithScaleFactor() {
        // Representing some byte sequence
        byte[] bytes = {0x12, 0x34};
        PrimitiveType type = PrimitiveType.builder().scaleFactor(2).build();
        when(ebcdicAsciiConvertor.convert(bytes)).thenReturn("1234");
        String result = picReader.readValue(bytes, type);
        assertEquals("\\u0012\\u00.94", result);
    }

    @Test
    void testReadValueWithoutScaleFactor() {
        // Representing some byte sequence
        byte[] bytes = {0x12, 0x34};
        PrimitiveType type = PrimitiveType.builder().scaleFactor(0).build();
        when(ebcdicAsciiConvertor.convert(bytes)).thenReturn("1234");
        String result = picReader.readValue(bytes, type);
        assertEquals("\\u0012\\u0094", result);
    }

    @Test
    void testInsertDotAtValidPosition() {
        String result = picReader.insertDotAtPosition(NUMBER, 3);
        assertEquals("123.456", result);
    }

    @Test
    void testInsertDotAtInvalidPositionFromEnd() {
        String result = picReader.insertDotAtPosition(NUMBER, 0);
        assertEquals(NUMBER, result);
    }

    @Test
    void testInsertDotAtPositionGreaterThanLength() {
        String result = picReader.insertDotAtPosition(NUMBER, 10);
        assertEquals(NUMBER, result);
    }

    @Test
    void testInsertDotAtEndPosition() {
        String result = picReader.insertDotAtPosition(NUMBER, 6);
        // This should add dot after first number in this case
        assertEquals(".123456", result);
    }
}
