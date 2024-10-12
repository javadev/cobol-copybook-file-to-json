package com.github.binarytojson.writer.factory;

import com.github.binarytojson.writer.JsonWriter;
import com.github.binarytojson.writer.Writer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The JsonWriterFactory class implements the WriterFactory interface to create instances of JsonWriter.
 */
public class JsonWriterFactory implements WriterFactory {

    /**
     * Creates a new JsonWriter instance with the specified OutputStream.
     *
     * @param os the OutputStream to which the JsonWriter will write data
     * @return a new JsonWriter instance.
     * @throws IOException if an I/O error occurs while creating the JsonWriter
     */
    @Override
    public Writer create(OutputStream os) throws IOException {
        return new JsonWriter(os);
    }
}
