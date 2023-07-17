package ru.vzotov.jqpdf.model;

import ru.vzotov.jqpdf.domain.AbstractRefOrName;
import ru.vzotov.jqpdf.domain.ColorSpace;
import ru.vzotov.jqpdf.domain.DecodeRange;
import ru.vzotov.jqpdf.domain.RefOrName;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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

    public Type type() {
        return Type.of((String) dict.get(PROP_TYPE));
    }

    public Subtype subtype() {
        return Subtype.of((String) dict.get(PROP_SUBTYPE));
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

    public RefOrName<ColorSpaceName, ColorSpace> colorSpace() {
        final String value = (String) dict.get(PROP_COLOR_SPACE);
        if (value == null) return null;

        final ColorSpaceName name = ColorSpaceName.of(value);
        return new ColorSpaceNameRefOrName(
                name == null ? value : null,
                name
        );
    }

    private static class ColorSpaceNameRefOrName extends AbstractRefOrName<ColorSpaceName, ColorSpace> {
        public ColorSpaceNameRefOrName(String ref, ColorSpaceName name) {
            super(ref, name);
        }

        @Override
        public ColorSpace resolve() {
            if(name() != null) {
                return new ColorSpace(name().defaultDecodeArray().get());
            }
            return null;
        }
    }

    public enum ColorSpaceName {
        DeviceGray("/DeviceGray", () -> new DecodeRange[]{
                new DecodeRange(0, 1),
        }),
        DeviceRGB("/DeviceRGB", () -> new DecodeRange[]{
                new DecodeRange(0, 1),
                new DecodeRange(0, 1),
                new DecodeRange(0, 1),
        }),
        DeviceCMYK("/DeviceCMYK", () -> new DecodeRange[]{
                new DecodeRange(0, 1),
                new DecodeRange(0, 1),
                new DecodeRange(0, 1),
                new DecodeRange(0, 1),
        }),
        ;
        private final String value;

        private final Supplier<DecodeRange[]> defaultDecodeArray;

        ColorSpaceName(String value, Supplier<DecodeRange[]> defaultDecodeArray) {
            this.value = value;
            this.defaultDecodeArray = defaultDecodeArray;
        }

        public String value() {
            return value;
        }

        public Supplier<DecodeRange[]> defaultDecodeArray() {
            return defaultDecodeArray;
        }

        private static final Map<String, ColorSpaceName> ENUM_MAP;

        static {
            Map<String, ColorSpaceName> map = new ConcurrentHashMap<>();
            for (ColorSpaceName instance : ColorSpaceName.values()) {
                map.put(instance.value().toLowerCase(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static ColorSpaceName of(String value) {
            return value == null ? null : ENUM_MAP.get(value.toLowerCase());
        }
    }

    public enum Type {
        METADATA("/Metadata"),
        XOBJECT("/XObject"),
        XREF("/XRef"),
        ;

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        private static final Map<String, Type> ENUM_MAP;

        static {
            Map<String, Type> map = new ConcurrentHashMap<>();
            for (Type instance : Type.values()) {
                map.put(instance.value().toLowerCase(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static Type of(String value) {
            return value == null ? null : ENUM_MAP.get(value.toLowerCase());
        }

    }

    public enum Subtype {
        IMAGE("/Image"),
        ;

        private final String value;

        Subtype(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        private static final Map<String, Subtype> ENUM_MAP;

        static {
            Map<String, Subtype> map = new ConcurrentHashMap<>();
            for (Subtype instance : Subtype.values()) {
                map.put(instance.value().toLowerCase(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static Subtype of(String value) {
            return value == null ? null : ENUM_MAP.get(value.toLowerCase());
        }
    }
}
