package com.github.binarytojson.reader.structure;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NameAndValues {
    private String name;
    private Object values;
}
