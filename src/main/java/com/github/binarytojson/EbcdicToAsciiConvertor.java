package com.github.binarytojson;

import com.github.binarytojson.reader.file.BufferedInputStreamReader;
import com.github.binarytojson.reader.file.IReader;
import com.github.binarytojson.reader.structure.StructureRecord;
import com.github.binarytojson.type.DataType;
import com.github.binarytojson.type.HeaderRecordDto;
import com.github.binarytojson.type.HeaderRecordType;
import com.github.binarytojson.type.PrimitiveType;
import com.github.binarytojson.writer.Writer;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EbcdicToAsciiConvertor {

    private static final String FILL_KEY = "FILL1";
    private static final String SEG_NAME_KEY = "SEGNAME";
    private static final String HEADER_NAME_SKIP = "SKIP";
    private static final List<PrimitiveType> FILL_AND_SEG_NAME =
            Arrays.asList(
                    PrimitiveType.builder()
                            .name(FILL_KEY)
                            .dataType(DataType.CHAR)
                            .level(1)
                            .length(6)
                            .build(),
                    PrimitiveType.builder()
                            .name(SEG_NAME_KEY)
                            .dataType(DataType.CHAR)
                            .level(1)
                            .length(8)
                            .build());

    private final BufferedInputStreamReader reader = new BufferedInputStreamReader();

    public void convert(
            InputStream inputStream,
            OutputStream os,
            List<HeaderRecordDto> headers,
            GenerationType generationType,
            Cache cache,
            Mode... modes)
            throws IOException {
        Iterable<byte[]> records =
                reader.readBinaryFile(new BufferedInputStream(inputStream, cache.getBufferSize()));
        Mode mode = modes.length == 0 ? Mode.WITH_ARRAY : modes[0];
        HeaderRecordDto headerRecordDto = headers.get(0);
        setFixedLengthIfNeeded(headerRecordDto, reader);
        try (Writer writer = generationType.getWriterFactory().create(os)) {
            writer.writeStartArray();
            int index = 0;
            int groupIndex = 0;
            List<StructureRecord> structureRecords = new ArrayList<>();
            String headerName =
                    headers.stream()
                            .filter(it -> it.getPrimitiveTypes().get(0).isRootElement())
                            .findFirst()
                            .map(it -> it.getPrimitiveTypes().get(0).getName())
                            .orElse(null);
            List<HeaderRecordDto> headersWithoutRoot =
                    headers.stream()
                            .filter(it -> !it.getPrimitiveTypes().get(0).isRootElement())
                            .collect(Collectors.toList());
            for (byte[] bytes : records) {
                HeaderRecordDto headerRecord = getHeaderRecordDto(headersWithoutRoot, index, bytes);
                List<PrimitiveType> primitiveTypes =
                        headerRecord.getPrimitiveTypes().stream()
                                .map(PrimitiveType::copy)
                                .collect(Collectors.toList());
                if (HEADER_NAME_SKIP.equals(primitiveTypes.get(0).getName())) {
                    index++;
                    continue;
                }
                StructureRecord structureRecord = new StructureRecord(bytes, primitiveTypes);
                if (HEADER_NAME_SKIP.equals(
                        headersWithoutRoot.get(0).getPrimitiveTypes().get(0).getName())) {
                    int level = primitiveTypes.get(0).getLevel();
                    if (level == 1 && !structureRecords.isEmpty()) {
                        writeGroup(mode, structureRecords, groupIndex, writer);
                        groupIndex++;
                    }
                    structureRecords.add(structureRecord);
                } else {
                    structureRecords.add(structureRecord);
                    groupIndex =
                            updateGroupIndex(
                                    headersWithoutRoot,
                                    headerName,
                                    mode,
                                    index,
                                    groupIndex,
                                    writer,
                                    structureRecords);
                }
                index++;
            }
            if (!structureRecords.isEmpty()) {
                writeGroup(mode, structureRecords, groupIndex, writer);
            }
            writer.writeEndArray();
        }
    }

    void setFixedLengthIfNeeded(HeaderRecordDto headerRecordDto, IReader reader) {
        if (HeaderRecordType.FIXED_FORMAT.equals(headerRecordDto.getRecordType())) {
            int length = calculateFixedLength(headerRecordDto.getPrimitiveTypes());
            reader.setFixedLength(length);
        } else {
            reader.setFixedLength(0);
        }
    }

    int calculateFixedLength(List<PrimitiveType> fields) {
        if (fields == null) {
            return 0;
        }
        int totalLength = 0;
        for (PrimitiveType field : fields) {
            totalLength += field.getLength();
            totalLength += calculateFixedLength(field.getFields());
        }
        return totalLength;
    }

    int updateGroupIndex(
            List<HeaderRecordDto> headers,
            String rootName,
            Mode mode,
            int index,
            int groupIndex,
            Writer writer,
            List<StructureRecord> structureRecords) {
        if ((index + 1) % headers.size() == 0) {
            if (groupIndex == 0) {
                writer.writeHeader(structureRecords, mode, rootName);
            }
            writer.writeObject(structureRecords, mode, rootName);
            groupIndex++;
            structureRecords.clear();
        }
        return groupIndex;
    }

    private void writeGroup(
            Mode mode, List<StructureRecord> structureRecords, int groupIndex, Writer writer) {
        StructureRecord structureRecordGroup =
                new StructureRecord(
                        convertToByteArray(
                                structureRecords.stream()
                                        .map(
                                                structureRecord ->
                                                        new AbstractMap.SimpleEntry<>(
                                                                structureRecord.getBytes(),
                                                                calculateFixedLength(
                                                                        structureRecord
                                                                                .getTypes())))
                                        .collect(Collectors.toList())),
                        structureRecords.stream()
                                .flatMap(it -> it.getTypes().stream())
                                .collect(Collectors.toList()));
        if (groupIndex == 0) {
            writer.writeHeader(Collections.singletonList(structureRecordGroup), mode, null);
        }
        writer.writeObject(Collections.singletonList(structureRecordGroup), mode, null);
        structureRecords.clear();
    }

    byte[] convertToByteArray(List<Map.Entry<byte[], Integer>> byteArrayList) {
        int totalLength = 0;
        for (Map.Entry<byte[], Integer> byteArray : byteArrayList) {
            totalLength += byteArray.getValue();
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (Map.Entry<byte[], Integer> byteArray : byteArrayList) {
            System.arraycopy(byteArray.getKey(), 0, result, currentIndex, byteArray.getValue());
            currentIndex += byteArray.getValue();
        }
        return result;
    }

    private HeaderRecordDto getHeaderRecordDto(
            List<HeaderRecordDto> headers, int index, byte[] bytes) {
        if (HEADER_NAME_SKIP.equals(headers.get(0).getPrimitiveTypes().get(0).getName())) {
            StructureRecord structureRecord = new StructureRecord(bytes, FILL_AND_SEG_NAME);
            Map<String, Object> map =
                    structureRecord.processList(FILL_AND_SEG_NAME, null, Mode.WITHOUT_ARRAY);
            Optional<HeaderRecordDto> foundHeader =
                    headers.stream()
                            .filter(
                                    it ->
                                            Objects.equals(
                                                    map.get(SEG_NAME_KEY),
                                                    it.getPrimitiveTypes().get(0).getName()))
                            .findFirst();
            return foundHeader.orElse(headers.get(index % headers.size()));
        }
        return headers.get(index % headers.size());
    }
}
