package com.github.binarytojson.writer;

import com.github.binarytojson.Mode;
import com.github.binarytojson.reader.structure.StructureRecord;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Writer interface defines methods for writing data to an output stream.
 */
public interface Writer extends Closeable {

    /**
     * Writes the header for the data structure.
     *
     * @param structureRecords the structure record describing the structure of the data
     * @param mode             the mode in which to write the header
     * @param rootName         the name of the root element
     */
    void writeHeader(List<StructureRecord> structureRecords, Mode mode, String rootName);

    /**
     * Writes an object of the data structure.
     *
     * @param structureRecords the structure record describing the structure of the data
     * @param mode             the mode in which to write the object
     * @param rootName         the name of the root element
     */
    void writeObject(List<StructureRecord> structureRecords, Mode mode, String rootName);

    /**
     * Writes the start of an array.
     */
    void writeStartArray();

    /**
     * Writes the end of an array.
     */
    void writeEndArray();

    /**
     * Converts a list of maps into a single map with string keys and object values.
     *
     * @param maps the list of maps to be converted
     * @param mode the mode in which to merge duplicate keys
     * @return a single map with string keys and object values
     */
    default Map<String, Object> getStringObjectMap(List<Map<String, Object>> maps, Mode mode) {
        if (maps.size() == 1) {
            return maps.get(0);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> map : maps) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                mergeMapEntry(mode, entry, result);
            }
        }
        return result;
    }

    /**
     * Merges a map entry into the result map based on the mode.
     *
     * @param mode   the mode in which to merge duplicate keys
     * @param entry  the map entry to merge
     * @param result the result map to merge into
     */
    @SuppressWarnings("unchecked")
    static void mergeMapEntry(Mode mode, Map.Entry<String, Object> entry, Map<String, Object> result) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (result.containsKey(key)) {
            if (mode == Mode.WITH_ARRAY) {
                Object existingValue = result.get(key);
                if (existingValue instanceof List) {
                    ((List<Object>) existingValue).add(value);
                } else {
                    List<Object> newValueList = new ArrayList<>();
                    newValueList.add(existingValue);
                    newValueList.add(value);
                    result.put(key, newValueList);
                }
            } else {
                int index = 1;
                while (result.containsKey(key + "(" + (index + 1) + ")")) {
                    index++;
                }
                result.put(key + "(" + (index + 1) + ")", value);
            }
        } else {
            result.put(key, value);
        }
    }
}
