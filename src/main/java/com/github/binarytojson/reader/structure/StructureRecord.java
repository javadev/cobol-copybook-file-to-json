package com.github.binarytojson.reader.structure;

import com.github.binarytojson.Mode;
import com.github.binarytojson.exception.UnsupportedTypeException;
import com.github.binarytojson.reader.type.BitReader;
import com.github.binarytojson.reader.type.CharReader;
import com.github.binarytojson.reader.type.FixedBinaryReader;
import com.github.binarytojson.reader.type.FixedReader;
import com.github.binarytojson.reader.type.PicReader;
import com.github.binarytojson.reader.type.TypeReader;
import com.github.binarytojson.type.DataType;
import com.github.binarytojson.type.PrimitiveType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The StructureRecord class represents a record with a byte array and provides methods to extract
 * data based on specified data types. It uses a map of data types to type readers for reading
 * values from the byte array.
 */
@SuppressWarnings("java:S1171")
@Getter
@RequiredArgsConstructor
@Slf4j
public class StructureRecord {

    private static final String FILL_1_KEY = "FILL1";
    private static final String FILL_2_KEY = "FILL2";
    /** The byte array representing the record. */
    private final byte @NonNull [] bytes;

    /**
     * The list of PrimitiveTypes comes from layout Can be modified during reading in case of VB and
     * keyword OCCURS (amount of repeats) in layout
     */
    private final @NonNull List<PrimitiveType> types;

    /** The map contains field values */
    private final Map<String, Object> fields = new LinkedHashMap<>();

    private int position;

    /** Resets the current position within the byte array. */
    public void resetPosition() {
        this.position = 0;
    }

    /** A map that associates each supported data type with its corresponding type reader. */
    private final Map<DataType, TypeReader> typeReaderMap = new HashMap<>();

    {
        typeReaderMap.put(DataType.BIT, new BitReader());
        typeReaderMap.put(DataType.CHAR, new CharReader());
        typeReaderMap.put(DataType.FIXED_BINARY, new FixedBinaryReader());
        typeReaderMap.put(DataType.FIXED, new FixedReader());
        typeReaderMap.put(DataType.PIC, new PicReader());
    }

    /**
     * Retrieves the data of the specified primitive type from the record starting at the specified
     * position.
     *
     * @param type the primitive type for which data needs to be extracted
     * @param mode the mode indicating whether to include array information
     * @return the extracted data as a string
     * @throws UnsupportedTypeException if the specified type is not supported
     */
    @SuppressWarnings("unchecked")
    public NameAndValues getData(PrimitiveType type, Mode mode) {
        DataType dt = type.getDataType();
        if (Objects.isNull(dt)) {
            return NameAndValues.builder().name(type.getName()).build();
        } else if (typeReaderMap.containsKey(dt)) {
            int toPosition = position + type.getLength();
            toPosition = Math.min(toPosition, getLen());
            String value =
                    typeReaderMap.get(dt).readValue(subArray(bytes, position, toPosition), type);
            int arrayIndex = type.getName().indexOf('(');
            String name;
            if (arrayIndex > 0 && mode == Mode.WITH_ARRAY) {
                name = type.getName().substring(0, arrayIndex);
                int index = getIndex(type.getName());
                if (index == 1) {
                    List<String> values = new ArrayList<>();
                    values.add(value);
                    fields.put(name, values);
                } else {
                    ((List<String>) fields.get(name)).add(value);
                }
            } else {
                name = type.getName();
                fields.put(type.getName(), value);
            }
            position += type.getLength();
            return NameAndValues.builder()
                    .name(name)
                    .values(arrayIndex > 0 ? fields.get(name) : value)
                    .build();
        } else {
            log.info("Unknown type : {}", type);
            throw new UnsupportedTypeException("Unknown type");
        }
    }

    private byte[] subArray(byte[] array, int startIndex, int endIndex) {
        int length = endIndex - startIndex;
        byte[] subArray = new byte[length];
        System.arraycopy(array, startIndex, subArray, 0, length);
        return subArray;
    }

    /**
     * Processes a list of PrimitiveType objects recursively.
     *
     * @param list the list of PrimitiveType objects to process
     * @param parent the parent PrimitiveType object
     * @param mode the mode indicating whether to include array information
     * @return a map containing the processed data
     */
    public Map<String, Object> processList(
            List<PrimitiveType> list, PrimitiveType parent, Mode mode) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < list.size(); index++) {
            PrimitiveType type = list.get(index);
            boolean readElement = true;
            if (fieldsNotEmpty(type)) {
                if (type.getAmount() != null) {
                    readElement =
                            processAmount(type, getAmountStr(parent, mode, type), list, index);
                }
                if (readElement) {
                    Map<String, Object> children = processList(type.getFields(), type, mode);
                    modifyTypeNameForArrayMode(mode, type);
                    addChildrenToResult(result, type, children);
                }

            } else {
                NameAndValues nameAndValues = getData(type, mode);
                result.put(nameAndValues.getName(), nameAndValues.getValues());
            }
        }
        result.remove(FILL_1_KEY);
        result.remove(FILL_2_KEY);
        return result;
    }

    /**
     * Modifies the type name to include array information based on the mode.
     *
     * @param mode the mode indicating whether to include array information
     * @param type the PrimitiveType object
     */
    private void modifyTypeNameForArrayMode(Mode mode, PrimitiveType type) {
        if (mode == Mode.WITH_ARRAY) {
            int startIndex = type.getName().indexOf('(');
            int commaIndex = type.getName().indexOf(',');
            if (startIndex > 0) {
                int endIndex = commaIndex > 0 ? commaIndex : startIndex;
                String newName = type.getName().substring(0, endIndex);
                if (commaIndex > 0) {
                    newName += ")";
                }
                type.setName(newName);
            }
        }
    }

    /**
     * Adds children to the result map under the appropriate key.
     *
     * @param result the result map
     * @param type the parent PrimitiveType object
     * @param children the children to be added
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addChildrenToResult(
            Map<String, Object> result, PrimitiveType type, Map<String, Object> children) {
        if (result.containsKey(type.getName())) {
            Object item = result.get(type.getName());
            if (item instanceof List) {
                ((List) item).add(children);
            } else {
                List<Object> items = new ArrayList<>();
                items.add(item);
                items.add(children);
                result.put(type.getName(), items);
            }
        } else {
            result.put(type.getName(), children);
        }
    }

    /**
     * Gets the amount string for the given PrimitiveType object.
     *
     * @param parent the parent PrimitiveType object
     * @param mode the mode indicating whether to include array information
     * @param type the PrimitiveType object
     * @return the amount string
     */
    private String getAmountStr(PrimitiveType parent, Mode mode, PrimitiveType type) {
        if (Objects.nonNull(parent)) {
            int index = getIndex(parent.getName());
            if (index != 0) {
                String amount = type.getAmount();
                int startIndex = amount.indexOf('(');
                int endIndex = amount.indexOf(')', startIndex);
                if (startIndex != -1 && endIndex != -1) {
                    type.setAmount(
                            amount.substring(0, startIndex + 1)
                                    + index
                                    + amount.substring(endIndex));
                }
            }
        }
        return this.getFieldValue(type.getAmount(), mode);
    }

    /**
     * Checks if the given PrimitiveType object has non-empty fields.
     *
     * @param type the PrimitiveType object to check
     * @return true if the fields are not empty, otherwise false
     */
    private boolean fieldsNotEmpty(PrimitiveType type) {
        return type.getFields() != null && !type.getFields().isEmpty();
    }

    /**
     * Processes the amount for the given PrimitiveType object.
     *
     * @param type the PrimitiveType object
     * @param amountStr the amount string
     * @param list the list of PrimitiveType objects
     * @param index the index of the current PrimitiveType object
     * @return true if the element should be read, otherwise false
     */
    private boolean processAmount(
            PrimitiveType type, String amountStr, List<PrimitiveType> list, int index) {
        int amount = Integer.parseInt(amountStr);
        if (amount == 0) {
            return false;
        } else {
            String name = type.getName();
            type.setAmount(null);
            type.setName(name + "(1)");
            for (int i = 1; i < amount; i++) {
                PrimitiveType newType = type.copy();
                newType.setName(name + "(" + (i + 1) + ")");
                list.add(index + i, newType);
            }
        }
        return true;
    }

    /**
     * Gets the length of the byte array representing the record.
     *
     * @return the length of the byte array
     */
    public int getLen() {
        return bytes.length;
    }

    /**
     * Gets the field value by name.
     *
     * @param name the name of the field
     * @param mode the mode indicating whether to include array information
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    public String getFieldValue(String name, Mode mode) {
        int arrayIndex = name.indexOf('(');
        String newName;
        if (arrayIndex > 0 && mode == Mode.WITH_ARRAY) {
            newName = name.substring(0, arrayIndex);
            int index = getIndex(name) - 1;
            List<String> values = (List<String>) fields.get(newName);
            return values.get(index);
        }
        return (String) fields.getOrDefault(name, name);
    }

    /**
     * Gets the index extracted from the given name.
     *
     * @param name the name containing an index in parentheses
     * @return the index
     */
    private int getIndex(String name) {
        int startIndex = name.indexOf('(');
        int endIndex = name.indexOf(')', startIndex);
        if (startIndex != -1 && endIndex != -1) {
            String indexStr = name.substring(startIndex + 1, endIndex);
            try {
                return Integer.parseInt(indexStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
