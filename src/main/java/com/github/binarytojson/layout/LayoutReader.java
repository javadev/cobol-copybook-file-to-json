package com.github.binarytojson.layout;

import com.github.binarytojson.exception.ReadConfigurationException;
import com.github.binarytojson.type.HeaderRecordDto;
import com.github.binarytojson.type.HeaderRecordType;
import com.github.binarytojson.type.PrimitiveType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for reading and processing configuration files.
 */
@Slf4j
public class LayoutReader {

    /**
     * Regular expression pattern for matching numbers enclosed in parentheses.
     */
    private static final Pattern PATTERN_NUMBER = Pattern.compile("\\((\\d+)\\)");
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\r?\\n");

    /**
     * Regular expression pattern for matching comments in a line.
     */
    private static final String COMMENTS_PATTERN = "/\\*.*?\\*/";

    /**
     * Pattern object for matching comments in a line.
     */
    private static final Pattern PATTERN_COMMENTS = Pattern.compile(COMMENTS_PATTERN, Pattern.DOTALL);

    /**
     * Regular expression pattern for matching spaces in a line.
     */
    private static final String SPACES_PATTERN = "\\s+";

    /**
     * Maximum length allowed for a line.
     */
    private static final int MAX_LINE_LENGTH = 72;

    private final LayoutRowParser layoutRowParser = new LayoutRowParser();

    /**
     * Reads all lines from a file, normalizes them, and returns a list of processed lines.
     *
     * @param fileName the name of the file to read
     * @return a list of processed lines from the file
     * @throws ReadConfigurationException if an error occurs while reading the file.
     */
    public List<HeaderRecordDto> readAllLinesFromFile(String fileName) {
        try {
            List<String> linesFromFile = replaceMultilineComments(
                    removeEmptyLines(
                            new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8)));

            List<List<String>> headerRecords = splitByRecord(linesFromFile);
            List<HeaderRecordDto> result = new ArrayList<>();
            for (List<String> headerRecord : headerRecords) {
                processRecord(headerRecord, result);
            }
            return result;
        } catch (IOException e) {
            throw new ReadConfigurationException("Error reading file", e);
        }
    }

    private void processRecord(List<String> headerRecord, List<HeaderRecordDto> result) {
        String headerLine = headerRecord.get(0);
        HeaderRecordType recordType = headerLine.contains(HeaderRecordType.FIXED_FORMAT.getValue()) ?
                HeaderRecordType.FIXED_FORMAT
                : HeaderRecordType.VARIABLE_FORMAT;
        Matcher matcher = PATTERN_NUMBER.matcher(headerLine);
        int count = matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
        List<PrimitiveType> types = stylization(headerRecord.stream()
                .map(this::normalizeLine)
                .flatMap(line -> layoutRowParser.parseRow(line, isHeaderRecord(line)).stream())
                .collect(Collectors.toList()));
        HeaderRecordDto header = new HeaderRecordDto(recordType, types);
        for (int i = 0; i < count; i++) {
            result.add(header);
        }
    }

    List<String> replaceMultilineComments(String input) {
        // Regex pattern to find multi-line comments
        Matcher matcher = PATTERN_COMMENTS.matcher(input);
        StringBuffer result = new StringBuffer();
        // Loop through all matches and convert each multi-line comment to a single-line comment
        while (matcher.find()) {
            String comment = matcher.group(0);
            // Remove newlines and extra spaces inside the comment
            String singleLineComment = comment.replaceAll("[\\n\\r]+", " ")
                    .replaceAll("/\\*|\\*/", "").trim();
            // Replace it with a single-line comment
            matcher.appendReplacement(result, "/* " + singleLineComment + " */");
        }
        // Append the remaining part of the content
        matcher.appendTail(result);
        return Arrays.asList(NEW_LINE_PATTERN.split(result.toString()));
    }

    String removeEmptyLines(String input) {
        return input.replaceAll("(?m)^\\s*$[\n\r]+", "");
    }

    /**
     * Normalizes a line by removing comments, extra spaces, and trimming.
     *
     * @param line the input line to be normalized
     * @return the normalized line
     */
    String normalizeLine(String line) {
        String normalized = line.length() > MAX_LINE_LENGTH ? line.substring(0, MAX_LINE_LENGTH) : line;
        return normalized.replaceAll(COMMENTS_PATTERN, "")
                .replaceAll(SPACES_PATTERN, " ")
                .trim();
    }

    /**
     * Split lines by header records.
     *
     * @param listFromFile the input lines from file
     * @return split into header records lines by keywords 0DCL|DCL|DECLARE
     */
    public List<List<String>> splitByRecord(List<String> listFromFile) {
        List<List<String>> parts = new ArrayList<>();
        List<String> currentPart = new ArrayList<>();
        boolean firstRecord = false;
        for (String line : listFromFile) {
            line = line.trim();
            if (isHeaderRecord(line)) {
                if (firstRecord && (!currentPart.isEmpty())) {
                    parts.add(currentPart);
                }
                currentPart = new ArrayList<>();
                firstRecord = true;
            }
            currentPart.add(line);
        }
        if (!currentPart.isEmpty()) {
            parts.add(currentPart);
        }
        return parts;
    }

    boolean isHeaderRecord(String line) {
        return line.matches("^(ROOT|0DCL|DCL|DECLARE)\\b.*$");
    }

    private List<PrimitiveType> stylization(List<PrimitiveType> primitiveTypes) {
        List<PrimitiveType> results = new ArrayList<>();
        List<PrimitiveType> newPrimitiveTypes = copyArrays(primitiveTypes);
        Deque<PrimitiveType> stack = new ArrayDeque<>();
        for (PrimitiveType pt : newPrimitiveTypes) {
            while (!stack.isEmpty() && stack.peek().getLevel() >= pt.getLevel()) {
                stack.pop();
            }
            if (stack.isEmpty()) {
                results.add(pt);
            } else {
                if (stack.peek().getFields() == null) {
                    stack.peek().setFields(new ArrayList<>());
                }
                stack.peek().getFields().add(pt);
            }
            stack.push(pt);
        }
        return results;
    }

    private List<PrimitiveType> copyArrays(List<PrimitiveType> primitiveTypes) {
        List<PrimitiveType> newPrimitiveTypes = new ArrayList<>();
        for (int i = 0; i < primitiveTypes.size(); i++) {
            PrimitiveType primitiveType = primitiveTypes.get(i);
            if (primitiveType.getArray1() > 0) {
                doCopyArrays(primitiveTypes, primitiveType, i, newPrimitiveTypes);
            } else {
                newPrimitiveTypes.add(primitiveType);
            }
        }
        return newPrimitiveTypes;
    }

    private void doCopyArrays(List<PrimitiveType> primitiveTypes, PrimitiveType primitiveType,
                              int i, List<PrimitiveType> newPrimitiveTypes) {
        int indexElement = getIndexElement(primitiveTypes, primitiveType, i);
        if (primitiveType.getArray2() > 0) {
            copyValuesArray2d(primitiveType, primitiveTypes, newPrimitiveTypes, i + 1, indexElement);
        } else {
            copyValuesArray1d(primitiveType, primitiveTypes, newPrimitiveTypes, i + 1, indexElement);
        }
    }

    private int getIndexElement(List<PrimitiveType> primitiveTypes, PrimitiveType primitiveType, int i) {
        int indexElement = i + 1;
        while (indexElement < primitiveTypes.size()
               && primitiveTypes.get(indexElement).getLevel() > primitiveType.getLevel()) {
            indexElement++;
        }
        return indexElement;
    }

    private void copyValuesArray1d(PrimitiveType primitiveType, List<PrimitiveType> primitiveTypes,
                                   List<PrimitiveType> newPrimitiveTypes, int indexFrom, int indexTo) {
        for (int index = 1; index <= primitiveType.getArray1(); index++) {
            newPrimitiveTypes.add(primitiveType.toBuilder().name(
                    primitiveType.getName() + "(" + index + ")").build());
            if (index < primitiveType.getArray1()) {
                for (int index2 = indexFrom; index2 < indexTo; index2++) {
                    if (primitiveTypes.get(index2).getArray1() > 0) {
                        doCopyArrays(primitiveTypes, primitiveTypes.get(index2), index2, newPrimitiveTypes);
                    } else {
                        newPrimitiveTypes.add(primitiveTypes.get(index2));
                    }
                }
            }
        }
    }

    private void copyValuesArray2d(PrimitiveType primitiveType, List<PrimitiveType> primitiveTypes,
                                   List<PrimitiveType> newPrimitiveTypes, int indexFrom, int indexTo) {
        for (int index = 1; index <= primitiveType.getArray1(); index++) {
            for (int index2 = 1; index2 <= primitiveType.getArray2(); index2++) {
                newPrimitiveTypes.add(
                        primitiveType.toBuilder()
                                .name(primitiveType.getName() + "(" + index + "," + index2 + ")").build());
                if (index < primitiveType.getArray1()
                    || index2 < primitiveType.getArray2()) {
                    for (int index3 = indexFrom; index3 < indexTo; index3++) {
                        newPrimitiveTypes.add(primitiveTypes.get(index3));
                    }
                }
            }
        }
    }
}
