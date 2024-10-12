package com.github.binarytojson;

import com.github.binarytojson.layout.LayoutReader;
import com.github.binarytojson.type.HeaderRecordDto;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
        // Set up command line options
        Options options = new Options();
        options.addRequiredOption("l", "layout", true, "Path to the layout file");
        options.addRequiredOption("s", "source", true, "Path to the source binary file");
        options.addOption("f", "format", true, "Output format (e.g., JSON)");
        options.addOption("t", "target", true, "Path to the output JSON file (default: output.json)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.info("Error parsing command line arguments: {}", e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("BinaryToJsonConverter", options);
            return;
        }

        String layoutFilePath = cmd.getOptionValue("l");
        String sourceFilePath = cmd.getOptionValue("s");
        // Default to JSON if not provided
        String outputFormat = cmd.getOptionValue("f", "JSON");
        // Default output path; can be changed as needed
        String jsonOutputPath = cmd.getOptionValue("t",
                outputFormat.equalsIgnoreCase("JSON") ? "output.json" : "output.csv");

        List<HeaderRecordDto> headerRecordDtos = layoutReader.readAllLinesFromFile(layoutFilePath);

        try (InputStream inputStream = Files.newInputStream(Paths.get(sourceFilePath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(jsonOutputPath))) {
            ebcdicToAsciiConvertor.convert(inputStream, outputStream, headerRecordDtos,
                    GenerationType.valueOf(outputFormat.toUpperCase()), Cache.TEN_MEGABYTES);
            log.info("{} file created successfully: {}", outputFormat.toUpperCase(), jsonOutputPath);
        }
    }
}
