package ru.vzotov.jqpdf.domain;

public record SampleModel(ColorSpace colorSpace, int bitsPerComponent) {
    public int bitsPerSample() {
        return colorSpace.components() * bitsPerComponent;
    }

    public int getLuminance(long data) {
        //todo: delegate to colorspace
        if (colorSpace.components() == 3) {
            final int mask = (0xFF >>> (8 - bitsPerComponent));
            int r = (int) (data) & mask;
            int g = (int) (data >>> bitsPerComponent) & mask;
            int b = (int) (data >>> (bitsPerComponent * 2)) & mask;
            return rgbLuminance(r, g, b);
        } else if (colorSpace.components() == 4) {
            final int mask = (0xFF >>> (8 - bitsPerComponent));
            int c = (int) (data) & mask;
            int m = (int) (data >>> bitsPerComponent) & mask;
            int y = (int) (data >>> (bitsPerComponent * 2)) & mask;
            int k = (int) (data >>> (bitsPerComponent * 3)) & mask;

            return 0xff - Math.min(0xff, (int) (0.3 * c + 0.59 * m + 0.11 * y) + k);
        }

        return data == 0 ? 0 : 0xff;
    }

    private static int rgbLuminance(int r, int g, int b) {
        return (int) (0.3 * r + 0.59 * g + 0.11 * b);
    }
}
