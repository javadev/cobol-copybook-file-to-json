package com.github.binarytojson.layout;

import com.github.binarytojson.type.HeaderRecordDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutReaderTest {

    private LayoutReader layoutReader;

    @BeforeEach
    void setUp() {
        layoutReader = new LayoutReader();
    }

    @Test
    void testNormalizeLine() {
        String input = "   DCL (123) /* comment */  ";
        String expected = "DCL (123)";
        String result = layoutReader.normalizeLine(input);
        assertEquals(expected, result);
    }

    @Test
    void testReplaceMultilineComments() {
        String input = "/* This is a comment\nline 1 */\nAnother line /* comment */";
        List<String> expected = Arrays.asList("/* This is a comment line 1 */", "Another line /* comment */");
        List<String> result = layoutReader.replaceMultilineComments(input);
        assertEquals(expected, result);
    }

    @Test
    void testRemoveEmptyLines() {
        String input = "\n\nline 1\n\nline 2\n";
        String expected = "line 1\nline 2\n";
        String result = layoutReader.removeEmptyLines(input);
        assertEquals(expected, result);
    }

    @Test
    void testIsHeaderRecord() {
        assertTrue(layoutReader.isHeaderRecord("DCL RECORD"));
        assertFalse(layoutReader.isHeaderRecord("Non-header line"));
    }

    @Test
    void testSplitByRecord() {
        List<String> input = Arrays.asList("DCL HEADER 1", "line 1", "DCL HEADER 2", "line 2");
        List<List<String>> expected = Arrays.asList(
                Arrays.asList("DCL HEADER 1", "line 1"),
                Arrays.asList("DCL HEADER 2", "line 2")
        );
        List<List<String>> result = layoutReader.splitByRecord(input);
        assertEquals(expected, result);
    }

    @Test
    void testReadAllLinesFromFile() throws IOException {
        // Mock file content
        String fileName = "testFile.txt";
        String fileContent = "DCL RECORD\nline 1\n/* comment */\nDCL RECORD 2\nline 2";

        // Mock file reading
        Files.write(Paths.get(fileName), fileContent.getBytes());

        // Execute method
        List<HeaderRecordDto> result = layoutReader.readAllLinesFromFile(fileName);

        // Check result
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Clean up
        Files.delete(Paths.get(fileName));
    }
}
