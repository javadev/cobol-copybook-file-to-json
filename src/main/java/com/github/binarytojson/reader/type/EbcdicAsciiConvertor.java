package com.github.binarytojson.reader.type;

import java.nio.charset.StandardCharsets;

public class EbcdicAsciiConvertor {

    private static final int NON_PRINTABLE_CHARACTERS_BELOW_32 = 32;
    private static final int NON_PRINTABLE_CHARACTERS_BEYOND_126 = 126;
    private static final int NON_PRINTABLE_CHARACTERS_BELOW_192 = 192;
    private static final int LENGTH_OF_UNICODE_PREFIX = 5;
    private static final int MASK_FOR_DEFINE_BYTE = 0xFF;

    public String convert(byte[] ebcdicBytes) {
        byte[] bytes = new byte[ebcdicBytes.length];
        int i = 0;
        for (int el : ebcdicBytes) {
            int symbol = EBCDIC_2_ASCII[el & MASK_FOR_DEFINE_BYTE];
            if (symbol < NON_PRINTABLE_CHARACTERS_BELOW_32
                    || (symbol > NON_PRINTABLE_CHARACTERS_BEYOND_126
                    && symbol < NON_PRINTABLE_CHARACTERS_BELOW_192)) {
                byte[] newBytes = new byte[bytes.length + LENGTH_OF_UNICODE_PREFIX];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
                bytes[i++] = '\\';
                bytes[i++] = 'u';
                bytes[i++] = '0';
                bytes[i++] = '0';
                bytes[i++] = (byte) Character.toUpperCase(Character.forDigit((symbol >> 4) & 0xF, 16));
                bytes[i++] = (byte) Character.toUpperCase(Character.forDigit(symbol & 0xF, 16));
            } else {
                bytes[i++] = (byte) symbol;
            }
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    /**
     * Predefined EBCDIC to ASCII conversion table.
     */
    static final int[] EBCDIC_2_ASCII = new int[]{
            0x0, 0x1, 0x2, 0x3,
            0x9C, 0x9, 0x86, 0x7F,
            0x97, 0x8D, 0x8E, 0xB,
            0xC, 0xD, 0xE, 0xF,
            0x10, 0x11, 0x12, 0x13,
            0x9D, 0xA, 0x8, 0x87,
            0x18, 0x19, 0x92, 0x8F,
            0x1C, 0x1D, 0x1E, 0x1F,
            0x80, 0x81, 0x82, 0x83,
            0x84, 0x85, 0x17, 0x1B,
            0x88, 0x89, 0x8A, 0x8B,
            0x8C, 0x5, 0x6, 0x7,
            0x90, 0x91, 0x16, 0x93,
            0x94, 0x95, 0x96, 0x4,
            0x98, 0x99, 0x9A, 0x9B,
            0x14, 0x15, 0x9E, 0x1A,
            0x20, 0xA0, 0xE2, 0xE4,
            0xE0, 0xE1, 0xE3, 0xE5,
            0xE7, 0xF1, 0xA2, 0x2E,
            0x3C, 0x28, 0x2B, 0x7C,
            0x26, 0xE9, 0xEA, 0xEB,
            0xE8, 0xED, 0xEE, 0xEF,
            0xEC, 0xDF, 0x21, 0x24,
            0x2A, 0x29, 0x3B, 0x5E,
            0x2D, 0x2F, 0xC2, 0xC4,
            0xC0, 0xC1, 0xC3, 0xC5,
            0xC7, 0xD1, 0xA6, 0x2C,
            0x25, 0x5F, 0x3E, 0x3F,
            0xF8, 0xC9, 0xCA, 0xCB,
            0xC8, 0xCD, 0xCE, 0xCF,
            0xCC, 0x60, 0x3A, 0x23,
            0x40, 0x27, 0x3D, 0x22,
            0xD8, 0x61, 0x62, 0x63,
            0x64, 0x65, 0x66, 0x67,
            0x68, 0x69, 0xAB, 0xBB,
            0xF0, 0xFD, 0xFE, 0xB1,
            0xB0, 0x6A, 0x6B, 0x6C,
            0x6D, 0x6E, 0x6F, 0x70,
            0x71, 0x72, 0xAA, 0xBA,
            0xE6, 0xB8, 0xC6, 0xA4,
            0xB5, 0x7E, 0x73, 0x74,
            0x75, 0x76, 0x77, 0x78,
            0x79, 0x7A, 0xA1, 0xBF,
            0xD0, 0x5B, 0xDE, 0xAE,
            0xAC, 0xA3, 0xA5, 0xB7,
            0xA9, 0xA7, 0xB6, 0xBC,
            0xBD, 0xBE, 0xDD, 0xA8,
            0xAF, 0x5D, 0xB4, 0xD7,
            0x7B, 0x41, 0x42, 0x43,
            0x44, 0x45, 0x46, 0x47,
            0x48, 0x49, 0xAD, 0xF4,
            0xF6, 0xF2, 0xF3, 0xF5,
            0x7D, 0x4A, 0x4B, 0x4C,
            0x4D, 0x4E, 0x4F, 0x50,
            0x51, 0x52, 0xB9, 0xFB,
            0xFC, 0xF9, 0xFA, 0xFF,
            0x5C, 0xF7, 0x53, 0x54,
            0x55, 0x56, 0x57, 0x58,
            0x59, 0x5A, 0xB2, 0xD4,
            0xD6, 0xD2, 0xD3, 0xD5,
            0x30, 0x31, 0x32, 0x33,
            0x34, 0x35, 0x36, 0x37,
            0x38, 0x39, 0xB3, 0xDB,
            0xDC, 0xD9, 0xDA, 0x9F
    };
}
