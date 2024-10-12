package com.github.binarytojson.type;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class PrimitiveType {
    @Setter
    private String name;
    private int level;
    @ToString.Exclude
    private int start;
    @ToString.Exclude
    private int numberOfBits;
    private int length;
    @ToString.Exclude
    private int digitsCount;
    @ToString.Exclude
    private int scaleFactor;
    @ToString.Exclude
    private boolean signed;
    private DataType dataType;
    @Setter
    private String amount;
    @Setter
    private List<PrimitiveType> fields;
    private int array1;
    private int array2;
    private boolean rootElement;

    public PrimitiveType copy() {
        return toBuilder()
                .fields(Objects.isNull(fields) ? null : fields.stream().map(PrimitiveType::copy)
                        .collect(Collectors.toList()))
                .build();
    }
}
