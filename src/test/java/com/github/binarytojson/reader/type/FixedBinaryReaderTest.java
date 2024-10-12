package com.github.binarytojson.reader.type;

import com.github.binarytojson.exception.UnsupportedTypeException;
import com.github.binarytojson.type.PrimitiveType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FixedBinaryReaderTest {

    @Test
    void testReadValueUnsigned() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        // Example byte array
        byte[] bytes = {0x01, 0x02};
        when(type.getDigitsCount()).thenReturn(16);
        when(type.isSigned()).thenReturn(false);
        when(type.getName()).thenReturn("unsigned16");

        String result = reader.readValue(bytes, type);

        // 0x0102 == 258 in decimal
        assertEquals("258", result);
    }

    @Test
    void testReadValueSignedPositive() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        // Example byte array
        byte[] bytes = {0x01, 0x02};
        when(type.getDigitsCount()).thenReturn(16);
        when(type.isSigned()).thenReturn(true);
        when(type.getName()).thenReturn("signed16");

        String result = reader.readValue(bytes, type);

        // 0x0102 == 258 in decimal, no sign change
        assertEquals("258", result);
    }

    @Test
    void testReadValueSignedNegative() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        // Signed byte with a negative sign
        byte[] bytes = {(byte) 0x81, 0x02};
        when(type.getDigitsCount()).thenReturn(16);
        when(type.isSigned()).thenReturn(true);
        when(type.getName()).thenReturn("signed16");

        String result = reader.readValue(bytes, type);

        // Sign is detected (-), value is calculated
        assertEquals("-258", result);
    }

    @Test
    void testUnsupportedTypeException() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        // Wrong length for 16 bits
        byte[] bytes = {0x01};
        when(type.getDigitsCount()).thenReturn(16);
        when(type.isSigned()).thenReturn(true);
        when(type.getName()).thenReturn("signed16");

        assertThrows(UnsupportedTypeException.class, () -> reader.readValue(bytes, type));
    }

    @Test
    void testGetControlLen16Bits() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        when(type.getDigitsCount()).thenReturn(16);
        int controlLen = reader.readValue(new byte[]{0x01, 0x02}, type).length();

        // For 16 bits, control length should be 2 bytes
        // "258" has 3 characters
        assertEquals(3, controlLen);
    }

    @Test
    void testGetControlLen32Bits() {
        FixedBinaryReader reader = new FixedBinaryReader();
        PrimitiveType type = mock(PrimitiveType.class);

        when(type.getDigitsCount()).thenReturn(32);
        String result = reader.readValue(new byte[]{0x00, 0x00, 0x01, 0x00}, type);

        // 0x00000100 == 256 in decimal
        assertEquals("256", result);
    }

    @Test
    void testReadValue_ValidInput() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {0x01, 0x02}; // 16 bits = 2 bytes
        PrimitiveType type = PrimitiveType.builder().name("FIXED BINARY").signed(false).digitsCount(16).build();
        String expected = "258"; // (0x01 * 256) + 0x02 = 258
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_ValidInput_32() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {0x01, 0x02, 0, 0}; // 32 bits = 4 bytes
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(false).digitsCount(32).build();
        String expected = "16908288"; // (0x01 * 256 * 65536) + 0x02 = 16908288
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_ValidInput_32Bits() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {0x01, 0x02, 0, 0}; // 32 bits = 4 bytes
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(false).digitsCount(32).build();
        String expected = "16908288"; // (0x01 * 256 * 65536) + 0x02 = 16908288
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_ValidInput_63_MinValue() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {(byte) 0xBF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(true).digitsCount(64).build();
        String expected = "-4611686018427387903";
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_ValidInput_63_MaxValue() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {(byte) 0x3F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(false).digitsCount(64).build();
        String expected = "4611686018427387903";
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_SignedInput() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {(byte) 0xFF, 0x7F}; // Signed input -32639
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(true).digitsCount(16).build();
        String expected = "-32639";
        assertEquals(expected, reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_InvalidInput() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {0x01, 0x02, 0x03}; // Invalid, expected length 2 bytes for 16 bits
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(false).digitsCount(16).build();
        assertThrows(UnsupportedTypeException.class, () -> reader.readValue(bytes, type));
    }

    @Test
    void testReadValue_ZeroInput() {
        FixedBinaryReader reader = new FixedBinaryReader();
        byte[] bytes = {0x00, 0x00}; // 16 bits = 2 bytes
        PrimitiveType type = PrimitiveType.builder()
                .name("FIXED BINARY").signed(false).digitsCount(16).build();
        String expected = "0"; // Zero value
        assertEquals(expected, reader.readValue(bytes, type));
    }

}
