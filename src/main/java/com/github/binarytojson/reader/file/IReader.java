package com.github.binarytojson.reader.file;

/** Interface for Reader with fixed/not fixed length functionality */
public interface IReader {

    /**
     * Sets the fixed length for reading binary records.
     *
     * @param fixedLength the fixed length to set. 0 - length depend on RDW
     */
    void setFixedLength(int fixedLength);
}
