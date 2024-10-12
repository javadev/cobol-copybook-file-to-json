package com.github.binarytojson.reader.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.github.binarytojson.utils.Constants.HEADER_WITH_RDW_LENGTH;

/**
 * Class for reading binary data from a BufferedInputStream.
 */
@Slf4j
public class BufferedInputStreamReader implements IReader {

    private RecordIterator iterator;

    /**
     * Reads binary data from the provided BufferedInputStream in fixed or variable length records.
     *
     * @param bufferedInputStream the BufferedInputStream from which to read binary data
     * @return an Iterable of byte arrays representing the binary records
     */
    public Iterable<byte[]> readBinaryFile(BufferedInputStream bufferedInputStream) {
        iterator = new RecordIterator(bufferedInputStream);
        return () -> iterator;
    }

    /**
     * Sets the fixed length for reading binary records.
     *
     * @param fixedLength the fixed length to set
     */
    @Override
    public void setFixedLength(int fixedLength) {
        iterator.modifyFixedLength(fixedLength);
    }

    /**
     * Inner class that implements the {@link Iterator} interface to iterate over binary records.
     */
    private static class RecordIterator implements Iterator<byte[]> {
        private final BufferedInputStream bufferedInputStream;
        private int fixedLength;

        /**
         * Constructs a {@link RecordIterator} with the provided {@link DataInputStream}.
         *
         * @param bufferedInputStream the {@link BufferedInputStream} from which to read binary data
         */
        public RecordIterator(BufferedInputStream bufferedInputStream) {
            this.bufferedInputStream = bufferedInputStream;
            this.fixedLength = 0;
        }

        /**
         * Modifies the fixed length for reading binary records.
         *
         * @param fixedLength the new fixed length to set
         */
        public void modifyFixedLength(int fixedLength) {
            this.fixedLength = fixedLength;
        }

        /**
         * Checks if there are more binary records available for reading.
         *
         * @return true if there are more records, false otherwise
         */
        @SneakyThrows
        @Override
        public boolean hasNext() {
            return bufferedInputStream.available() > 0;
        }

        /**
         * Reads the next binary record.
         *
         * @return the next binary record as a byte array
         */
        @SneakyThrows
        @Override
        public byte[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Not enough bytes left in the buffer for a complete record");
            }
            if (fixedLength > 0) {
                byte[] recordData = new byte[fixedLength];
                if (bufferedInputStream.read(recordData) != -1) {
                    return recordData;
                }
            } else {
                if (bufferedInputStream.available() >= HEADER_WITH_RDW_LENGTH) {
                    int rdw = readShort(bufferedInputStream);
                    int recordLength = rdw + readShort(bufferedInputStream) - HEADER_WITH_RDW_LENGTH;
                    byte[] recordData = new byte[recordLength];
                    if (bufferedInputStream.read(recordData) != -1) {
                        return recordData;
                    }
                }
            }
            return new byte[]{};
        }

        /**
         * Reads a short integer from the input stream.
         *
         * @param inputStream the input stream from which to read the short integer
         * @return the short integer read from the input stream
         * @throws IOException if an I/O error occurs during the conversion process
         */
        private int readShort(InputStream inputStream) throws IOException {
            int ch1 = inputStream.read();
            int ch2 = inputStream.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException();
            }
            return (ch1 << 8) + ch2;
        }
    }
}
