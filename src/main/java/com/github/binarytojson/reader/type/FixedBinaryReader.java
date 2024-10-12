package com.github.binarytojson.reader.type;

import com.github.binarytojson.exception.UnsupportedTypeException;
import com.github.binarytojson.type.PrimitiveType;

import static com.github.binarytojson.utils.Constants.MASK_FOR_DEFINE_BYTE;
import static com.github.binarytojson.utils.Constants.MASK_WITHOUT_SIGN;
import static com.github.binarytojson.utils.Constants.SIGN_DETECTION_MASK;
import static com.github.binarytojson.utils.Constants.SIGN_MINUS;

public class FixedBinaryReader implements TypeReader {

    private static final long LONG = 8L;
    private static final int TWO_BYTES = 16;
    private static final int FOUR_BYTES = 32;
    private static final int EIGHT_BYTES = 64;
    private static final int CONTROL_LEN_TWO = 2;
    private static final int CONTROL_LEN_FOUR = 4;
    private static final int CONTROL_LEN_EIGHT = 8;

    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        int digitsCount = type.getDigitsCount();
        int controlLen = getControlLen(digitsCount);
        if (bytes.length != controlLen) {
            throw new UnsupportedTypeException(String.format("Could not read var : %s", type.getName()));
        }
        long result = 0;
        String sign = "";
        for (int i = 0; i < bytes.length; i++) {
            int currentByte = bytes[i];
            if (i == 0 && type.isSigned()) {
                sign = (currentByte & SIGN_DETECTION_MASK) != 0 ? SIGN_MINUS : "";
                currentByte &= MASK_WITHOUT_SIGN;
            }
            result |= (long) (currentByte & MASK_FOR_DEFINE_BYTE) << (bytes.length - i - 1) * LONG;
        }
        return sign + result;
    }

    private int getControlLen(int digitsCount) {
        if (digitsCount <= TWO_BYTES) {
            return CONTROL_LEN_TWO;
        } else if (digitsCount <= FOUR_BYTES) {
            return CONTROL_LEN_FOUR;
        } else if (digitsCount <= EIGHT_BYTES) {
            return CONTROL_LEN_EIGHT;
        } else {
            return 0;
        }
    }
}
