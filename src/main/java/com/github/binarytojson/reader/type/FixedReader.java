package com.github.binarytojson.reader.type;

import com.github.binarytojson.type.PrimitiveType;

import static com.github.binarytojson.utils.Constants.DOT;
import static com.github.binarytojson.utils.Constants.MASK_FOR_DEFINE_HIGH_NIBBLE;
import static com.github.binarytojson.utils.Constants.MASK_FOR_DEFINE_LOW_NIBBLE;
import static com.github.binarytojson.utils.Constants.SHIFT_FOR_HIGH_NIBBLE;
import static com.github.binarytojson.utils.Constants.SIGN_MINUS;
import static com.github.binarytojson.utils.Constants.VALUE_MINUS;

public class FixedReader implements TypeReader {

    @Override
    public String readValue(byte[] bytes, PrimitiveType type) {
        int digitsCount = type.getDigitsCount();
        int scaleFactor = type.getScaleFactor();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int firstDigit = (bytes[i] & MASK_FOR_DEFINE_HIGH_NIBBLE) >> SHIFT_FOR_HIGH_NIBBLE;
            int secondDigit = bytes[i] & MASK_FOR_DEFINE_LOW_NIBBLE;
            if (i == bytes.length - 1 && secondDigit == VALUE_MINUS) {
                sb.append(firstDigit);
                sb.insert(0, SIGN_MINUS);
            } else {
                sb.append(firstDigit);
                setDecimalPoint(digitsCount, scaleFactor, sb);
                if (i <= bytes.length - 1) {
                    sb.append(secondDigit);
                }
                setDecimalPoint(digitsCount, scaleFactor, sb);
            }
        }
        return sb.toString();
    }

    private static void setDecimalPoint(int digitsCount, int scaleFactor, StringBuilder sb) {
        if (scaleFactor == 0) {
            return;
        }
        int decimalPointPosition = digitsCount - scaleFactor;
        if (sb.length() == decimalPointPosition) {
            sb.append(DOT);
        }
    }
}
