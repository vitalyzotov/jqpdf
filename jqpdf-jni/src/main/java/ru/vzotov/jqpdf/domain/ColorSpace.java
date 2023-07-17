package ru.vzotov.jqpdf.domain;

import java.util.Objects;

public class ColorSpace {

    private final DecodeRange[] decode;

    public ColorSpace(DecodeRange[] decode) {
        Objects.requireNonNull(decode);
        this.decode = decode;
    }

    public DecodeRange[] decode() {
        return decode;
    }

    public int components() {
        return decode.length;
    }
}
