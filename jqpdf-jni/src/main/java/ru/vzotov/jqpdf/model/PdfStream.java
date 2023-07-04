package ru.vzotov.jqpdf.model;

import java.util.Map;

public record PdfStream(
        String data,
        Map<String, ?> dict
) {
    public static final String PROP_TYPE = "/Type";
    public static final String PROP_SUBTYPE = "/Subtype";
    public static final String PROP_WIDTH = "/Width";
    public static final String PROP_HEIGHT = "/Height";
    public static final String PROP_COLOR_SPACE = "/ColorSpace";
    public static final String PROP_BPC = "/BitsPerComponent";

    public String type() {
        return (String) dict.get(PROP_TYPE);
    }

    public String subtype() {
        return (String) dict.get(PROP_SUBTYPE);
    }

    public int width() {
        return ((Number) dict.get(PROP_WIDTH)).intValue();
    }

    public int height() {
        return ((Number) dict.get(PROP_HEIGHT)).intValue();
    }

    public int bitsPerComponent() {
        return ((Number) dict.get(PROP_BPC)).intValue();
    }


}
