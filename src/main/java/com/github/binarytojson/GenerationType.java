package com.github.binarytojson;

import com.github.binarytojson.writer.factory.CsvWriterFactory;
import com.github.binarytojson.writer.factory.JsonCompactWriterFactory;
import com.github.binarytojson.writer.factory.JsonWriterFactory;
import com.github.binarytojson.writer.factory.WriterFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The GenerationType enum represents different types of data generation formats, along with their
 * corresponding WriterFactory implementations.
 */
@AllArgsConstructor
@Getter
public enum GenerationType {

    /** JSON generation type using JsonWriterFactory. */
    JSON(new JsonWriterFactory()),

    /** Compact JSON generation type using JsonCompactWriterFactory. */
    JSON_COMPACT(new JsonCompactWriterFactory()),

    /** CSV generation type using CsvWriterFactory. */
    CSV(new CsvWriterFactory());

    private final WriterFactory writerFactory;
}
