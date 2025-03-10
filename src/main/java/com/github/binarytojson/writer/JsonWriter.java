package com.github.binarytojson.writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.binarytojson.Mode;
import com.github.binarytojson.reader.structure.StructureRecord;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/** The JsonWriter class implements the Writer interface to write data in JSON format. */
public class JsonWriter implements Writer {

    /** The JSON factory to create JSON generators. */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /** The default pretty printer for formatting JSON output. */
    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    /** The object mapper to serialize Java objects into JSON. */
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);

    /** The JsonGenerator used for writing JSON data. */
    protected final JsonGenerator jsonGenerator;

    /**
     * Constructs a JsonWriter object with the provided output stream.
     *
     * @param os the output stream to write JSON data to
     * @throws IOException if an I/O error occurs while creating the JsonGenerator
     */
    public JsonWriter(OutputStream os) throws IOException {
        jsonGenerator = JSON_FACTORY.createGenerator(os);
        jsonGenerator.setPrettyPrinter(PRETTY_PRINTER);
        jsonGenerator.setCodec(OBJECT_MAPPER);
    }

    /**
     * Writes the header information based on the specified list of PrimitiveTypes and
     * StructureRecord.
     *
     * @param structureRecords the list of structure records to be written
     * @param mode the mode in which to process the records
     * @param rootName the name of the root element
     */
    @Override
    public void writeHeader(List<StructureRecord> structureRecords, Mode mode, String rootName) {
        // nothing to do for json
    }

    /**
     * Writes the provided structure records as JSON objects.
     *
     * @param structureRecords the list of structure records to be written
     * @param mode the mode in which to process the records
     * @param rootName the name of the root element
     */
    @SneakyThrows
    @Override
    public void writeObject(List<StructureRecord> structureRecords, Mode mode, String rootName) {
        List<Map<String, Object>> maps =
                structureRecords.stream()
                        .map(it -> it.processList(it.getTypes(), null, mode))
                        .collect(Collectors.toList());
        Map<String, Object> result = getStringObjectMap(maps, mode);
        if (Objects.nonNull(rootName)) {
            result = Collections.singletonMap(rootName, result);
        }
        jsonGenerator.writeObject(result);
    }

    /** Writes the start of an array. */
    @SneakyThrows
    @Override
    public void writeStartArray() {
        jsonGenerator.writeStartArray();
    }

    /** Writes the end of an array. */
    @SneakyThrows
    @Override
    public void writeEndArray() {
        jsonGenerator.writeEndArray();
    }

    /**
     * Closes the underlying JsonGenerator.
     *
     * @throws IOException if an I/O error occurs while closing the JsonGenerator
     */
    @Override
    public void close() throws IOException {
        jsonGenerator.close();
    }
}
