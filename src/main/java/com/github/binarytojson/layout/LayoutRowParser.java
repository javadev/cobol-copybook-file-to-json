package com.github.binarytojson.layout;

import com.github.binarytojson.type.DataType;
import com.github.binarytojson.type.PrimitiveType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * The LayoutRowParser class is responsible for parsing a line of text representing a row in a data
 * file and extracting relevant information such as level, name, and data type.
 */
@Slf4j
public class LayoutRowParser {
    private static final String DEFAULT_NUMBER = "0";
    private static final int GROUP_TWO = 2;
    private static final int REPLACED_PATTERN_PAST_INDEX = 2;

    /** Pattern to match the layout row name. */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("(\\d+)\\s+([$\\w#]+)(\\((\\d+)\\))?(\\((\\d+),(\\d+)\\))?");

    /** Pattern to match the occurs (OCCURS:12 or OCCURS:#SNAP) */
    private static final Pattern OCCURS_PATTERN = Pattern.compile("OCCURS:(.+?),");

    /** Pattern to match the root element in layout rows. */
    private static final Pattern ROOT_PATTERN = Pattern.compile("ROOT\\s+(.+?),");

    /** List of data types to check for parsing. */
    private static final List<DataType> TYPES_TO_CHECK =
            Arrays.asList(
                    DataType.BIT,
                    DataType.CHAR,
                    DataType.FIXED,
                    DataType.FIXED_BINARY,
                    DataType.PIC);

    /**
     * Parses the given layout row and extracts primitive types.
     *
     * @param line the layout row to parse
     * @param headerRecord a boolean indicating whether the row is a header record
     * @return a list of primitive types extracted from the layout row
     */
    public List<PrimitiveType> parseRow(String line, boolean headerRecord) {
        Matcher nameMatcher = NAME_PATTERN.matcher(line);
        if (nameMatcher.find()) {
            Result result = getResult(nameMatcher, headerRecord);

            // Check possible data types
            for (DataType typeToCheck : TYPES_TO_CHECK) {
                // Regular expression pattern for checking different data types
                for (String alias : typeToCheck.getAliases()) {
                    List<PrimitiveType> primitiveTypes =
                            getPrimitiveTypes(
                                    line,
                                    typeToCheck,
                                    alias,
                                    result.getQuantity(),
                                    result.getName(),
                                    result.getLevel());
                    if (!primitiveTypes.isEmpty()) {
                        return primitiveTypes;
                    }
                }
            }
            Matcher occursMatcher = OCCURS_PATTERN.matcher(line);
            String amount = null;
            if (occursMatcher.find()) {
                amount = occursMatcher.group(1);
            }
            return Collections.singletonList(
                    PrimitiveType.builder()
                            .name(result.getName())
                            .amount(amount)
                            .level(result.getLevel())
                            .array1(result.getArray1())
                            .array2(result.getArray2())
                            .build());
        } else {
            Matcher rootMatcher = ROOT_PATTERN.matcher(line);
            if (rootMatcher.find()) {
                return Collections.singletonList(
                        PrimitiveType.builder()
                                .name(rootMatcher.group(1))
                                .rootElement(true)
                                .build());
            }
            return Collections.emptyList();
        }
    }

    private Result getResult(Matcher nameMatcher, boolean headerRecord) {
        int level = Integer.parseInt(nameMatcher.group(1));
        String name = nameMatcher.group(2);

        int quantity =
                Objects.nonNull(nameMatcher.group(4)) ? Integer.parseInt(nameMatcher.group(4)) : 1;
        int array1;
        if (Objects.nonNull(nameMatcher.group(4)) && !headerRecord) {
            array1 = Integer.parseInt(nameMatcher.group(4));
        } else if (Objects.nonNull(nameMatcher.group(6))) {
            array1 = Integer.parseInt(nameMatcher.group(6));
        } else {
            array1 = 0;
        }
        int array2 =
                Objects.nonNull(nameMatcher.group(7)) ? Integer.parseInt(nameMatcher.group(7)) : 0;
        return new Result(level, name, quantity, array1, array2);
    }

    @Value
    private static class Result {
        int level;
        String name;
        int quantity;
        int array1;
        int array2;
    }

    private List<PrimitiveType> getPrimitiveTypes(
            String line, DataType typeToCheck, String alias, int quantity, String name, int level) {
        Pattern typePattern = Pattern.compile(alias);
        Matcher typeMatcher = typePattern.matcher(line);
        if (typeMatcher.find()) {
            List<PrimitiveType> primitiveTypes = new ArrayList<>();
            for (int index = 0; index < quantity; index++) {
                primitiveTypes.add(
                        getPrimitiveType(
                                typeToCheck,
                                typeMatcher,
                                quantity > 1 ? String.format("%s(%d)", name, index + 1) : name,
                                level));
            }
            return primitiveTypes;
        }
        return Collections.emptyList();
    }

    private PrimitiveType getPrimitiveType(
            DataType typeToCheck, Matcher typeMatcher, String name, int level) {
        String numericGroup = typeMatcher.groupCount() == 0 ? DEFAULT_NUMBER : typeMatcher.group(1);
        String scaleFactorGroup =
                (typeMatcher.groupCount() >= GROUP_TWO)
                                && (!"".equals(typeMatcher.group(GROUP_TWO)))
                        ? typeMatcher.group(GROUP_TWO)
                        : "0";
        int digitsCount;
        if (DataType.PIC == typeToCheck) {
            String numericGroupWithoutBrackets = replaceN9Pattern(numericGroup);
            digitsCount = numericGroupWithoutBrackets.replace("V", "").length();
            if (numericGroupWithoutBrackets.contains("V")) {
                scaleFactorGroup =
                        String.valueOf(
                                numericGroupWithoutBrackets.length()
                                        - numericGroupWithoutBrackets.indexOf('V')
                                        - 1);
            }
        } else {
            digitsCount = Integer.parseInt(numericGroup);
        }
        int len = calculateLength(typeToCheck, digitsCount);
        return PrimitiveType.builder()
                .name(name)
                .level(level)
                .length(len)
                .dataType(typeToCheck)
                .digitsCount(digitsCount)
                .numberOfBits(typeToCheck == DataType.BIT ? digitsCount : 0)
                .scaleFactor(Integer.parseInt(scaleFactorGroup))
                .build();
    }

    /**
     * Calculates the length of the data based on the data type and digits count.
     *
     * @param typeToCheck the data type to be checked
     * @param digitsCount the number of digits
     * @return the calculated length of the data
     */
    private int calculateLength(DataType typeToCheck, int digitsCount) {
        if (DataType.CHAR.equals(typeToCheck)) {
            return digitsCount;
        }
        if (DataType.FIXED_BINARY.equals(typeToCheck) || DataType.BIT.equals(typeToCheck)) {
            return (digitsCount % 8 == 0) ? digitsCount / 8 : digitsCount / 8 + 1;
        }
        if (DataType.FIXED.equals(typeToCheck)) {
            return (digitsCount % 2 == 0) ? digitsCount / 2 : digitsCount / 2 + 1;
        }
        if (DataType.PIC.equals(typeToCheck)) {
            return digitsCount;
        }
        return 0;
    }

    String replaceN9Pattern(String input) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '(') {
                int start = i + 1;
                // Find closing parenthesis and then '9'
                int end = input.indexOf(')', start);
                i = replacePattern(input, end, start, builder, i);
            } else {
                builder.append(input.charAt(i));
                i++;
            }
        }
        return builder.toString();
    }

    private int replacePattern(String input, int end, int start, StringBuilder builder, int i) {
        if (end != -1 && end + 1 < input.length() && input.charAt(end + 1) == '9') {
            // Extract the number between parentheses
            String numberStr = input.substring(start, end);
            try {
                int number = Integer.parseInt(numberStr);
                // Replace (n)9 with 999
                for (int index = 0; index < Math.max(0, number); index++) {
                    builder.append("9");
                }
            } catch (NumberFormatException e) {
                // If parsing the number fails, append as is
                builder.append('(').append(numberStr).append(")9");
            }
            // Move index past the replaced pattern
            i = end + REPLACED_PATTERN_PAST_INDEX;
        } else {
            builder.append(input.charAt(i));
            i++;
        }
        return i;
    }
}
