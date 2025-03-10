package com.github.binarytojson.writer.factory;

import com.github.binarytojson.writer.JsonCompactWriter;
import com.github.binarytojson.writer.Writer;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The JsonCompactWriterFactory class implements the WriterFactory interface to create instances of
 * JsonCompactWriter.
 */
public class JsonCompactWriterFactory implements WriterFactory {

    /**
     * Creates a new JsonCompactWriter instance with the specified OutputStream.
     *
     * @param os the OutputStream to which the JsonCompactWriter will write data
     * @return a new JsonCompactWriter instance
     * @throws IOException if an I/O error occurs while creating the JsonCompactWriter
     */
    @Override
    public Writer create(OutputStream os) throws IOException {
        return new JsonCompactWriter(os);
    }
}
