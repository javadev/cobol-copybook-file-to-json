package com.github.binarytojson.utils;

/** Utility class containing constants used in the EBCDIC to ASCII transformer. */
public class Constants {

    private Constants() {}

    /** Mask to define a byte (8 bits). */
    public static final int MASK_FOR_DEFINE_BYTE = 0xFF;

    /** Mask to define the high nibble (4 bits) of a byte. */
    public static final int MASK_FOR_DEFINE_HIGH_NIBBLE = 0xF0;

    /** Mask to define the low nibble (4 bits) of a byte. */
    public static final int MASK_FOR_DEFINE_LOW_NIBBLE = 0x0F;

    /** String representation of the minus sign. */
    public static final String SIGN_MINUS = "-";

    /** String representation of the decimal point. */
    public static final char DOT = '.';

    /** Integer value representing the minus sign in EBCDIC. */
    public static final int VALUE_MINUS = 0xD;

    /** Mask used for sign detection (bitwise AND to check the sign bit). */
    public static final int SIGN_DETECTION_MASK = 0b10000000;

    /** Mask used to remove the sign bit, leaving only the magnitude bits. */
    public static final int MASK_WITHOUT_SIGN = 0b01111111;

    /** Mask used to SHIFT */
    public static final int SHIFT_FOR_HIGH_NIBBLE = 4;

    /** header with rdw length */
    public static final int HEADER_WITH_RDW_LENGTH = 4;
}
