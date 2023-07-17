package ru.vzotov.jqpdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import ru.vzotov.jqpdf.domain.ColorSpace;
import ru.vzotov.jqpdf.domain.RefOrName;
import ru.vzotov.jqpdf.domain.SampleModel;
import ru.vzotov.jqpdf.model.PdfObject;
import ru.vzotov.jqpdf.model.PdfStream;
import ru.vzotov.jqpdf.model.QPdfJson2;
import ru.vzotov.jqpdf.model.QPdfObject;
import ru.vzotov.jqpdf.model.QPdfObjects;
import ru.vzotov.jqpdf.utils.Bits;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class MainPdf {

    static class BitsLuminanceSource extends LuminanceSource {
        private final Bits matrix;
        private final SampleModel sampleModel;

        protected BitsLuminanceSource(Bits matrix, int width, int height, SampleModel sampleModel) {
            super(width, height);
            this.matrix = matrix;

            this.sampleModel = sampleModel;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            final int w = getWidth();
            final int bitsPerSample = sampleModel.bitsPerSample();
            final int dataPerRow = w * bitsPerSample;
            final int bitsPerRow = dataPerRow + ((8 - dataPerRow % 8) & 7);
            row = row == null || row.length < w ? new byte[w] : row;
            for (int x = 0; x < w; x++) {
                final int index = y * bitsPerRow + x * bitsPerSample;
                final long data = matrix.get(index, index + bitsPerSample);
                row[x] = (byte) sampleModel.getLuminance(data);
            }
            return row;
        }

        @Override
        public byte[] getMatrix() {
            int w = getWidth();
            int h = getHeight();
            byte[] result = new byte[w * h];

            final int bitsPerSample = sampleModel.bitsPerSample();
            final int dataPerRow = w * bitsPerSample;
            final int bitsPerRow = dataPerRow + ((8 - dataPerRow % 8) & 7);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    final int index = y * bitsPerRow + x * bitsPerSample;
                    final long data = matrix.get(index, index + bitsPerSample);

                    result[y * w + x] = (byte) sampleModel.getLuminance(data);
                }
            }
            return result;
        }
    }

    public static BufferedImage saveImage(LuminanceSource source) {
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = source.getWidth();
        int matrixHeight = source.getHeight();
        BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixHeight);
        // Paint and save the image using the ByteMatrix


        final byte[] matrix = source.getMatrix();
        for (int x = 0; x < matrixWidth; x++) {
            for (int y = 0; y < matrixHeight; y++) {
                final int v = Byte.toUnsignedInt(matrix[y * matrixWidth + x]);
                graphics.setColor(new Color(v, v, v));
                graphics.fillRect(x, y, 1, 1);
            }
        }
        return image;

    }

    public static void process(String key, PdfStream stream) throws IOException {
        int width = stream.width();
        int height = stream.height();

        final byte[] bytes = Base64.getDecoder().decode(stream.data());
        final ColorSpace colorSpace = Optional.ofNullable(stream.colorSpace()).map(RefOrName::resolve).orElse(null);
        if (colorSpace == null) {
            System.out.println("Color space not supported");
            return;
        }

        final Bits source = Bits.valueOf(bytes);
        final LuminanceSource ls = new BitsLuminanceSource(source, width, height, new SampleModel(colorSpace, stream.bitsPerComponent()));
        BinaryBitmap bmp = new BinaryBitmap(new HybridBinarizer(ls));

        try {
            Result result = new QRCodeReader().decode(bmp);
            System.out.println(key + "::" + result.getText());
            ImageIO.write(saveImage(ls), "png", Paths.get(key.replace(':', '_') + ".png").toFile());
        } catch (NotFoundException e) {
            //System.out.println("QR not found");
            ImageIO.write(saveImage(ls), "png", Paths.get(key.replace(':', '_') + "_empty.png").toFile());
        } catch (ChecksumException | FormatException e) {
            e.printStackTrace();
        }


    }

    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            System.out.println("Input pdf file required");
            System.exit(1);
        }

        final String inputFile = args[0];

        System.out.println("Java Library path = " + System.getProperty("java.library.path"));

        final JQPDF jqpdf = new JQPDF();
        final byte[] bytes = Files.readAllBytes(Path.of(inputFile));
        final StringBuilder builder = new StringBuilder();
        jqpdf.pdfToJson(bytes, (data) -> {
            final String s = new String(data, StandardCharsets.US_ASCII);
            System.out.println(s);
            builder.append(s);
        });

        final ObjectMapper mapper = new ObjectMapper();
        final QPdfJson2 json = mapper.readValue(builder.toString(), QPdfJson2.class);

        for (QPdfObject obj : json.qpdf()) {
            if (obj instanceof QPdfObjects all) {
                for (Map.Entry<String, PdfObject> entry : all.entrySet()) {
                    Optional.ofNullable(entry.getValue().stream())
                            .filter(s -> PdfStream.Subtype.IMAGE.equals(s.subtype()))
                            .ifPresent(s -> {
                                try {
                                    process(entry.getKey(), s);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }
        }

    }
}
