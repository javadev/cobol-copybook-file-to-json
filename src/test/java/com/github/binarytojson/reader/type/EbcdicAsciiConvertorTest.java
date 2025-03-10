package com.github.binarytojson.reader.type;

import static com.github.binarytojson.reader.type.EbcdicAsciiConvertor.EBCDIC_2_ASCII;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EbcdicAsciiConvertorTest {

    private final EbcdicAsciiConvertor convertor = new EbcdicAsciiConvertor();

    @Test
    void testConvertNormalCharacters() {
        // EBCDIC for 'A', 'B', 'C'
        byte[] ebcdicBytes = {(byte) 0xC1, (byte) 0xC2, (byte) 0xC3};
        String expected = "ABC";
        String actual = convertor.convert(ebcdicBytes);
        assertEquals(expected, actual, "Conversion of normal characters failed");
    }

    @Test
    void testConvertNonPrintableCharacters() {
        // EBCDIC for non-printable characters
        byte[] ebcdicBytes = {0x00, 0x01, 0x02};
        // Expected unicode representation
        String expected = "\\u0000\\u0001\\u0002";
        String actual = convertor.convert(ebcdicBytes);
        assertEquals(expected, actual, "Conversion of non-printable characters failed");
    }

    @Test
    void testConvertMixedCharacters() {
        // EBCDIC for 'A', non-printable, 'B'
        byte[] ebcdicBytes = {(byte) 0xC1, 0x00, (byte) 0xC2};
        // Expected unicode representation
        String expected = "A\\u0000B";
        String actual = convertor.convert(ebcdicBytes);
        assertEquals(expected, actual, "Conversion of mixed characters failed");
    }

    @Test
    void testConvertEmptyArray() {
        // Empty EBCDIC byte array
        byte[] ebcdicBytes = {};
        // Expected empty string
        String expected = "";
        String actual = convertor.convert(ebcdicBytes);
        assertEquals(expected, actual, "Conversion of empty array failed");
    }

    @Test
    void testConvertAllNonPrintableCharacters() {
        // EBCDIC bytes from 0x00 to 0x7F
        byte[] ebcdicBytes = new byte[128];
        for (int i = 0; i < 128; i++) {
            ebcdicBytes[i] = (byte) i;
        }
        String actual = convertor.convert(ebcdicBytes);
        // Generate expected result by manually mapping non-printable characters
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            int i1 = EBCDIC_2_ASCII[i & 0xFF];
            if (i1 < 32 || (i1 > 126 && i1 < 192)) {
                expected.append("\\u00");
                expected.append(String.format("%02X", i1));
            } else {
                expected.append((char) i1);
            }
        }
        assertEquals(
                expected.toString(), actual, "Conversion of all non-printable characters failed");
    }
}
