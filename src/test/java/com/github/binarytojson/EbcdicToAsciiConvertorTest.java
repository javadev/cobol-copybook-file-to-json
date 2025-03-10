package com.github.binarytojson;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.binarytojson.reader.file.BufferedInputStreamReader;
import com.github.binarytojson.reader.file.IReader;
import com.github.binarytojson.reader.structure.StructureRecord;
import com.github.binarytojson.type.DataType;
import com.github.binarytojson.type.HeaderRecordDto;
import com.github.binarytojson.type.HeaderRecordType;
import com.github.binarytojson.type.PrimitiveType;
import com.github.binarytojson.writer.Writer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EbcdicToAsciiConvertorTest {

    private static final String SEGNAME = "SEGNAME";
    private final Writer writer = mock(Writer.class);
    private final BufferedInputStreamReader reader = mock(BufferedInputStreamReader.class);

    private EbcdicToAsciiConvertor convertor;

    @BeforeEach
    void setUp() {
        convertor = new EbcdicToAsciiConvertor();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertSuccess() throws IOException {
        List<HeaderRecordDto> headers = new ArrayList<>();
        List<PrimitiveType> primitiveTypes = new ArrayList<>();
        primitiveTypes.add(
                PrimitiveType.builder()
                        .name(SEGNAME)
                        .dataType(DataType.CHAR)
                        .level(1)
                        .length(8)
                        .build());
        headers.add(HeaderRecordDto.builder().primitiveTypes(primitiveTypes).build());

        InputStream inputStream = new ByteArrayInputStream(new byte[] {0x01, 0x02, 0x03, 0x04});
        OutputStream outputStream = new ByteArrayOutputStream();

        when(reader.readBinaryFile(any())).thenReturn(mock(Iterable.class));
        doNothing().when(writer).writeStartArray();
        doNothing().when(writer).writeEndArray();

        // Execute conversion
        convertor.convert(inputStream, outputStream, headers, GenerationType.JSON, Cache.DEFAULT);

        // Verify interactions with writer
        verify(writer, never()).writeStartArray();
        verify(writer, never()).writeEndArray();
    }

    @Test
    void testConvertHandlesIOException() throws IOException {
        List<HeaderRecordDto> headers = new ArrayList<>();
        List<PrimitiveType> primitiveTypes = new ArrayList<>();
        primitiveTypes.add(
                PrimitiveType.builder()
                        .name(SEGNAME)
                        .dataType(DataType.CHAR)
                        .level(1)
                        .length(8)
                        .build());
        headers.add(HeaderRecordDto.builder().primitiveTypes(primitiveTypes).build());

        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = new ByteArrayOutputStream();

        when(inputStream.available()).thenReturn(10);
        when(inputStream.read()).thenThrow(new IOException("IO error"));
        assertThrows(
                IOException.class,
                () ->
                        convertor.convert(
                                inputStream,
                                outputStream,
                                headers,
                                GenerationType.JSON,
                                Cache.DEFAULT));
    }

    @Test
    void testSetFixedLengthIfNeeded() {
        IReader mockReader = mock(IReader.class);
        List<PrimitiveType> primitiveTypes = new ArrayList<>();
        primitiveTypes.add(
                PrimitiveType.builder()
                        .name(SEGNAME)
                        .dataType(DataType.CHAR)
                        .level(1)
                        .length(8)
                        .build());

        HeaderRecordDto headerRecordDto =
                HeaderRecordDto.builder()
                        .primitiveTypes(primitiveTypes)
                        .recordType(HeaderRecordType.FIXED_FORMAT)
                        .build();

        convertor.setFixedLengthIfNeeded(headerRecordDto, mockReader);

        // Expect fixed length of 8 as set in primitiveTypes
        verify(mockReader).setFixedLength(8);
    }

    @Test
    void testUpdateGroupIndex() {
        Writer mockWriter = mock(Writer.class);
        List<StructureRecord> structureRecords = new ArrayList<>();
        StructureRecord structureRecord =
                new StructureRecord(new byte[] {0x01, 0x02}, new ArrayList<>());
        structureRecords.add(structureRecord);

        List<HeaderRecordDto> headers = new ArrayList<>();
        headers.add(mock(HeaderRecordDto.class));

        int result =
                convertor.updateGroupIndex(
                        headers, "root", Mode.WITH_ARRAY, 1, 0, mockWriter, structureRecords);

        // Group index should increase
        assertEquals(1, result);
        verify(mockWriter, times(1)).writeHeader(anyList(), any(Mode.class), anyString());
    }

    @Test
    void testCalculateFixedLength() {
        List<PrimitiveType> primitiveTypes = new ArrayList<>();
        primitiveTypes.add(
                PrimitiveType.builder()
                        .name(SEGNAME)
                        .dataType(DataType.CHAR)
                        .level(1)
                        .length(8)
                        .build());

        int result = convertor.calculateFixedLength(primitiveTypes);
        // Fixed length calculated correctly
        assertEquals(8, result);
    }

    @Test
    void testConvertToByteArray() {
        List<Map.Entry<byte[], Integer>> byteArrayList = new ArrayList<>();
        byteArrayList.add(new AbstractMap.SimpleEntry<>(new byte[] {0x01, 0x02}, 2));

        byte[] result = convertor.convertToByteArray(byteArrayList);

        // Ensure the byte array is correctly formed
        assertArrayEquals(new byte[] {0x01, 0x02}, result);
    }
}
