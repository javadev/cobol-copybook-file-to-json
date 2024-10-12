package com.github.binarytojson.writer.factory;

import com.github.binarytojson.writer.Writer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The WriterFactory interface defines a method for creating instances of Writer implementations.
 */
public interface WriterFactory {

    /**
     * Creates a new Writer instance with the specified OutputStream.
     *
     * @param os the OutputStream to which the Writer will write data
     * @return a new Writer instance
     * @throws IOException if an I/O error occurs while creating the Writer.
     */
    Writer create(OutputStream os) throws IOException;
}
