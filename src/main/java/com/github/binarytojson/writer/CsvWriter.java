package com.github.binarytojson.writer;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.binarytojson.Mode;
import com.github.binarytojson.reader.structure.StructureRecord;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/** The CsvWriter class provides functionality for writing data to a CSV format. */
public class CsvWriter implements Writer {

    /** The CSV factory to create CSV generators. */
    private static final CsvFactory CSV_FACTORY = new CsvFactory();

    /** The default pretty printer for formatting CSV output. */
    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    /** The CSV mapper to serialize Java objects into CSV. */
    private static final CsvMapper CSV_MAPPER = new CsvMapper();

    /** The custom CSV schema to define the structure of the CSV output. */
    private static final CsvSchema CUSTOM_SCHEMA =
            CsvSchema.builder().setColumnSeparator('|').build();

    /** The CSV generator used for writing CSV data. */
    private final CsvGenerator csvGenerator;

    /**
     * Constructs a CsvWriter with the specified OutputStream.
     *
     * @param os the OutputStream to write CSV data to
     * @throws IOException if an I/O error occurs while creating the CsvWriter
     */
    public CsvWriter(OutputStream os) throws IOException {
        csvGenerator = CSV_FACTORY.createGenerator(os);
        csvGenerator.setPrettyPrinter(PRETTY_PRINTER);
        csvGenerator.setCodec(CSV_MAPPER);
        csvGenerator.setSchema(CUSTOM_SCHEMA);
        csvGenerator.enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS);
    }

    /**
     * Writes the header information based on the specified list of PrimitiveTypes and
     * StructureRecord.
     *
     * @param structureRecords the list of structure records
     * @param mode the mode to determine the data transformation
     * @param rootName the name of the root element
     */
    @SneakyThrows
    @Override
    public void writeHeader(List<StructureRecord> structureRecords, Mode mode, String rootName) {
        writeObjectInternal(true, structureRecords, mode, rootName);
    }

    /**
     * Writes an object based on the specified list of PrimitiveTypes and StructureRecord.
     *
     * @param structureRecords the list of structure records
     * @param mode the mode to determine the data transformation
     * @param rootName the name of the root element
     */
    @SneakyThrows
    @Override
    public void writeObject(List<StructureRecord> structureRecords, Mode mode, String rootName) {
        writeObjectInternal(false, structureRecords, mode, rootName);
    }

    private void writeObjectInternal(
            boolean addHeader, List<StructureRecord> structureRecords, Mode mode, String rootName)
            throws IOException {
        List<Map<String, Object>> maps =
                structureRecords.stream()
                        .map(
                                it -> {
                                    it.resetPosition();
                                    return it.processList(it.getTypes(), null, mode);
                                })
                        .collect(Collectors.toList());
        Map<String, Object> result = getStringObjectMap(maps, mode);
        if (Objects.nonNull(rootName)) {
            result = Collections.singletonMap(rootName, result);
        }
        List<String> data = new ArrayList<>();
        for (Map.Entry<String, Object> entry : convert(result, "")) {
            if (addHeader) {
                data.add(entry.getKey());
            } else {
                data.add(String.valueOf(entry.getValue()));
            }
        }
        csvGenerator.writeArray(data.toArray(new String[0]), 0, data.size());
    }

    /**
     * Converts a nested map to a list of map entries.
     *
     * @param map the nested map to convert
     * @param path the current path of the nested map
     * @return a list of map entries
     */
    @SuppressWarnings({"unchecked"})
    private List<Map.Entry<String, Object>> convert(Map<String, Object> map, String path) {
        List<Map.Entry<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) value;
                if (!valueMap.isEmpty()) {
                    resultList.addAll(convert(valueMap, fullPath));
                }
            } else if (value instanceof List) {
                List<?> valueList = (List<?>) value;
                if (!valueList.isEmpty() && valueList.get(0) instanceof Map) {
                    resultList.addAll(
                            valueList.stream()
                                    .flatMap(
                                            it ->
                                                    convert((Map<String, Object>) it, fullPath)
                                                            .stream())
                                    .collect(Collectors.toList()));
                }
            } else {
                resultList.add(new AbstractMap.SimpleEntry<>(fullPath, value));
            }
        }
        return resultList;
    }

    /** Writes the start of an array. */
    @SneakyThrows
    @Override
    public void writeStartArray() {
        csvGenerator.writeStartArray();
    }

    /** Writes the end of an array. */
    @SneakyThrows
    @Override
    public void writeEndArray() {
        csvGenerator.writeEndArray();
    }

    /**
     * Closes the underlying CsvGenerator.
     *
     * @throws IOException if an I/O error occurs while closing the CsvGenerator
     */
    @Override
    public void close() throws IOException {
        csvGenerator.close();
    }
}
