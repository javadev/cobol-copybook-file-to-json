# COBOL Copybook File to JSON Converter

[![Maven Central](https://img.shields.io/maven-central/v/com.github.javadev/cobol-copybook-file-to-json.svg)](https://central.sonatype.com/artifact/com.github.javadev/cobol-copybook-file-to-json/1.0)
[![Java CI](https://github.com/javadev/cobol-copybook-file-to-json/actions/workflows/maven.yml/badge.svg)](https://github.com/javadev/cobol-copybook-file-to-json/actions/workflows/maven.yml)

## Description
Easily convert COBOL copybook data to JSON format with this lightweight library, simplifying data handling and improving interoperability for your applications. Perfect for developers looking to streamline data processing!

## Usage

```
usage: java -jar cobol-copybook-file-to-json-1.0-all.jar [-f <arg>] -l <arg> -s <arg> [-t <arg>]

Converts data from a COBOL copybook file into a readable format such as CSV or JSON.

 -f,--format <arg>   Output formats (csv,json,json_compact; separate with comma)
 -l,--layout <arg>   Path to the layout file
 -s,--source <arg>   Path to the source binary file
 -t,--target <arg>   Base path for the output files (default: output)
```

## Example

```
java -jar target/cobol-copybook-file-to-json-1.0-all.jar \
     -s src/test/resources/data/sku.dat \
     -l src/test/resources/layout/sku.txt \
     -t sku \
     -f json,csv
```

This command converts the COBOL copybook data from `sku.dat` using the layout file `sku.txt` and outputs the results as `sku.json` and `sku.csv`.
