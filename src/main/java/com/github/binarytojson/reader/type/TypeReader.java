package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

public interface TypeReader {

    String readValue(byte[] bytes, PrimitiveType type);
}
