package com.github.binarytojson;

import com.github.binarytojson.layout.LayoutReader;
import com.github.binarytojson.type.HeaderRecordDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryToJsonConverter {
    private static final LayoutReader layoutReader = new LayoutReader();
    private static final EbcdicToAsciiConvertor ebcdicToAsciiConvertor = new EbcdicToAsciiConvertor();

    public static void main(String[] args) throws IOException {
        String filePath = "src/test/resources/data/sku.dat";
        String jsonOutputPath = "output.json";
        List<HeaderRecordDto> headerRecordDtos = layoutReader
                .readAllLinesFromFile("src/test/resources/layout/sku.txt");

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(jsonOutputPath))) {
            ebcdicToAsciiConvertor.convert(inputStream, outputStream, headerRecordDtos,
                    GenerationType.JSON, Cache.TEN_MEGABYTES);
            log.info("JSON file created successfully: {}", jsonOutputPath);
        }
    }
}
