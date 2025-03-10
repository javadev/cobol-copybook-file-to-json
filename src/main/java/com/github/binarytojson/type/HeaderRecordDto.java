package com.github.binarytojson.type;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Data Transfer Object representing a header record with information about the data structure. */
@Builder
@AllArgsConstructor
@Getter
public class HeaderRecordDto {

    /** The type of header record. */
    private HeaderRecordType recordType;

    /** The list of primitive types describing the data structure. */
    private List<PrimitiveType> primitiveTypes;
}
