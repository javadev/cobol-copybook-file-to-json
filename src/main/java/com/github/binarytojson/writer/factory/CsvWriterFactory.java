package com.github.binarytojson.writer.factory;

import com.github.binarytojson.writer.CsvWriter;
import com.github.binarytojson.writer.Writer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The CsvWriterFactory class implements the WriterFactory interface to create instances of CsvWriter.
 */
public class CsvWriterFactory implements WriterFactory {

    /**
     * Creates a new CsvWriter instance with the specified OutputStream.
     *
     * @param os the OutputStream to which the CsvWriter will write data
     * @return a new CsvWriter instance
     * @throws IOException if an I/O error occurs while creating the CsvWriter
     */
    @Override
    public Writer create(OutputStream os) throws IOException {
        return new CsvWriter(os);
    }
}
