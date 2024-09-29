package com.github.binarytojson;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BinaryToJsonConverterTest {

    @Test
    public void testReadFixedString() {
        ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes());
        String result = BinaryToJsonConverter.readFixedString(buffer, 5);
        assert "Hello".equals(result);
    }

    @Test
    void testParseBinaryFile() throws IOException {
        byte[] data = createTestData();
        try (BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(data));
             BufferedWriter writer = new BufferedWriter(new FileWriter("output_test.json"))) {
            BinaryToJsonConverter.parseBinaryFile(inputStream, writer);
        }

        // Verify that the JSON file is created correctly
        // (this could be enhanced to read the output file and assert its contents)
        // verify(writer, atLeastOnce()).write(anyString());
    }

    private byte[] createTestData() {
        ByteBuffer buffer = ByteBuffer.allocate(300); // Adjust size according to the layout
        buffer.putInt(20230101); // EXTRACT_DATE
        buffer.putInt(1200000); // EXTRACT_TIME
        buffer.put("ITEM123".getBytes(StandardCharsets.UTF_8)); // ITEM
        buffer.put("LOC01".getBytes(StandardCharsets.UTF_8)); // LOC
        buffer.putInt(30); // DRPCOVDUR
        buffer.put((byte) 1); // EXTERNALSKUSW
        buffer.putInt(15); // PLANDUR
        buffer.putInt(5); // PLANLEADTIME
        buffer.putFloat(100.5f); // OH
        buffer.putInt(10); // OHPOST
        buffer.putInt(20); // SSCOV
        buffer.putFloat(15.5f); // STATSS
        buffer.putFloat(10.0f); // MINDRPQTY
        buffer.putFloat(5.0f); // INCDRPQTY
        buffer.putInt(25); // CURCOVDUR
        buffer.putFloat(150.5f); // MAXPROJOH
        buffer.putInt(20230105); // MAXPROJOHDATE
        buffer.put("CH".getBytes(StandardCharsets.UTF_8)); // CHANNEL
        buffer.putInt(20230102); // DRPTIMEFENCEDATE
        buffer.put("VEND".getBytes(StandardCharsets.UTF_8)); // VENDOR
        buffer.putFloat(200.5f); // MAXSS
        buffer.put("SUBMODE".getBytes(StandardCharsets.UTF_8)); // SUB_MODE
        buffer.put("USA".getBytes(StandardCharsets.UTF_8)); // COUNTRY_CODE
        buffer.put((byte) 1); // CPFR
        buffer.putInt(10); // TRUCKLOAD_MIN
        buffer.putInt(20); // TRUCKLOAD_MAX
        buffer.putFloat(5.0f); // BACKORDERS
        buffer.putInt(20230103); // STOCKOUTDATE
        buffer.putInt(5); // STOCKOUTDUR
        buffer.putFloat(0.0f); // STOCKOUTQTY
        buffer.putFloat(3.0f); // MINSS
        buffer.put("CM1".getBytes(StandardCharsets.UTF_8)); // CM_NUMBER
        buffer.put("CAT1".getBytes(StandardCharsets.UTF_8)); // FINELINE_CAT
        buffer.put("SCA".getBytes(StandardCharsets.UTF_8)); // SCA_NUMBER
        buffer.put("Comment example text.".getBytes(StandardCharsets.UTF_8)); // COMMENTS

        return buffer.array();
    }


    @Test
    public void testMain() throws IOException {
        String filePath = "test.bin";
        // Create a temporary binary file for testing
        try (FileWriter fileWriter = new FileWriter(filePath);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write("test binary data");
        }

        // Mock the BufferedWriter and invoke the main method
        BinaryToJsonConverter.main(new String[]{filePath});
        
        // Verify if the JSON file is created (you can check file existence)
        File jsonFile = new File("output.json");
        assertTrue(jsonFile.exists());

        // Clean up
        jsonFile.delete();
        new File(filePath).delete();
    }
}
