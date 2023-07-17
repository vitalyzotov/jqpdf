package ru.vzotov.jqpdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import ru.vzotov.jqpdf.model.PdfObject;
import ru.vzotov.jqpdf.model.PdfStream;
import ru.vzotov.jqpdf.model.QPdfJson2;
import ru.vzotov.jqpdf.model.QPdfObject;
import ru.vzotov.jqpdf.model.QPdfObjects;

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

public class MainPdf {

    static class DirectLuminanceSource extends LuminanceSource {

        private final BitMatrix matrix;

        protected DirectLuminanceSource(BitMatrix matrix) {
            super(matrix.getWidth(), matrix.getHeight());
            this.matrix = matrix;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            int w = getWidth();
            row = row == null || row.length < w ? new byte[w] : row;
            for (int i = 0; i < w; i++) {
                row[i] = (matrix.get(i, y) ? 0 : (byte) 0xff);
            }
            return row;
        }

        @Override
        public byte[] getMatrix() {
            int w = getWidth();
            int h = getHeight();
            byte[] result = new byte[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    result[y * w + x] = (matrix.get(x, y) ? 0 : (byte) 0xff);
                }
            }
            return result;
        }
    }

    public static BufferedImage saveImage(BitMatrix byteMatrix) {
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        return image;

    }


    public static void process(PdfStream stream) throws IOException {
        int width = stream.width();
        int height = stream.height();
        //final byte[] bytes = stream.data().getBytes(StandardCharsets.US_ASCII);
        final byte[] bytes = Base64.getDecoder().decode(stream.data());
        System.out.println("data size = " + bytes.length);
        System.out.println("w = " + width);
        System.out.println("h = " + height);


//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = (byte) (Integer.reverse(Byte.toUnsignedInt(bytes[i])) >> 24);
//        }

        BitSource source = new BitSource(bytes);
        BitMatrix matrix = new BitMatrix(width, height);
        for (int row = 0; row < height; row++) {
            for(int col =0; col < width; col++) {
                final int bits = source.readBits(1);
                if((bits & 0x1) > 0) {
                    matrix.unset(col, row);
                } else {
                    matrix.set(col, row);
                }
            }

            int align = source.available() % 8;
            if (align > 0) {
                source.readBits(align);
            }

            //matrix.setRow(row, array);
        }

        ImageIO.write(saveImage(matrix), "png", Paths.get("out.png").toFile());

        final DirectLuminanceSource ls = new DirectLuminanceSource(matrix);
        BinaryBitmap bmp = new BinaryBitmap(new HybridBinarizer(ls));


        try {
            Result result = new QRCodeReader().decode(bmp);
            System.out.println(result.getText());
        } catch (NotFoundException e) {
            System.out.println("QR not found");
        } catch (ChecksumException | FormatException e) {
            e.printStackTrace();
        }


    }

    public static void main(String... args) throws IOException {
        if(args.length < 1) {
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
            builder.append(s);
        });

        ObjectMapper mapper = new ObjectMapper();
        final QPdfJson2 json = mapper.readValue(builder.toString(), QPdfJson2.class);

        for (QPdfObject obj : json.qpdf()) {
            if (obj instanceof QPdfObjects all) {
                for (Map.Entry<String, PdfObject> entry : all.entrySet()) {
                    final PdfStream stream = entry.getValue().stream();
                    if (stream != null) {
                        if ("/XObject".equals(stream.type()) && "/Image".equals(stream.subtype())) {
                            process(stream);
                        }
                    }
                }
            }
        }
    }
}
