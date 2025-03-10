package com.github.binarytojson;

import com.github.binarytojson.layout.LayoutReader;
import com.github.binarytojson.type.HeaderRecordDto;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class BinaryToJsonConverter {
    private static final String HEADER =
            "Converts data from a binary file into a readable format such as CSV, JSON, or TXT.";
    private static final String FOOTER = "\nKindly submit any issues to support.";
    private static final LayoutReader layoutReader = new LayoutReader();
    private static final EbcdicToAsciiConvertor ebcdicToAsciiConvertor =
            new EbcdicToAsciiConvertor();

    public static void main(String[] args) {
        // Set up command line options
        Options options = new Options();
        options.addRequiredOption("l", "layout", true, "Path to the layout file");
        options.addRequiredOption("s", "source", true, "Path to the source binary file");
        options.addOption(
                "f", "format", true, "Output formats (csv,json,json_compact; separate with comma)");
        options.addOption("t", "target", true, "Base path for the output files (default: output)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.info("Error parsing command line arguments: {}", e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "java -jar cobol-copybook-file-to-json-1.0-all.jar",
                    HEADER,
                    options,
                    FOOTER,
                    true);
            return;
        }

        String layoutFilePath = cmd.getOptionValue("l");
        String sourceFilePath = cmd.getOptionValue("s");
        String outputFormats = cmd.getOptionValue("f", "json").toLowerCase();
        String baseOutputPath = cmd.getOptionValue("t", "output");

        Set<String> formatList = new LinkedHashSet<>(Arrays.asList(outputFormats.split(",")));
        List<HeaderRecordDto> headerRecordDtos = layoutReader.readAllLinesFromFile(layoutFilePath);

        Path sourcePath = Paths.get(sourceFilePath);

        // Process each format
        for (String format : formatList) {
            switch (format) {
                case "json":
                    processFile(
                            sourcePath,
                            baseOutputPath + ".json",
                            headerRecordDtos,
                            GenerationType.JSON);
                    break;
                case "json_compact":
                    processFile(
                            sourcePath,
                            baseOutputPath + ".compact.json",
                            headerRecordDtos,
                            GenerationType.JSON_COMPACT);
                    break;
                case "csv":
                    processFile(
                            sourcePath,
                            baseOutputPath + ".csv",
                            headerRecordDtos,
                            GenerationType.CSV);
                    break;
                default:
                    log.error("Unsupported format: {}", format);
                    break;
            }
        }
    }

    private static void processFile(
            Path sourcePath,
            String outputPath,
            List<HeaderRecordDto> headerRecordDtos,
            GenerationType type) {
        try (InputStream inputStream = Files.newInputStream(sourcePath);
                OutputStream outputStream = Files.newOutputStream(Paths.get(outputPath))) {
            ebcdicToAsciiConvertor.convert(
                    inputStream, outputStream, headerRecordDtos, type, Cache.TEN_MEGABYTES);
            log.info("{} file created successfully: {}", type.name(), outputPath);
        } catch (IOException e) {
            log.error("Error creating {} file: {}", type.name(), e.getMessage());
        }
    }
}
