package com.github.binarytojson.writer;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The JsonCompactWriter class extends JsonWriter to provide functionality for writing data to a JSON format in a compact manner.
 */
public class JsonCompactWriter extends JsonWriter {

    /**
     * Constructs a JsonCompactWriter with the specified OutputStream.
     *
     * @param os the OutputStream to write JSON data to
     * @throws IOException if an I/O error occurs while creating the JsonCompactWriter
     */
    public JsonCompactWriter(OutputStream os) throws IOException {
        super(os);
        jsonGenerator.setPrettyPrinter(getPrettyPrinter());
    }

    /**
     * Retrieves the PrettyPrinter used for formatting JSON output in a compact manner.
     *
     * @return the PrettyPrinter instance with compact formatting settings
     */
    private PrettyPrinter getPrettyPrinter() {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(new DefaultPrettyPrinter.NopIndenter());
        printer.indentArraysWith(new DefaultPrettyPrinter.NopIndenter());
        return printer;
    }
}
