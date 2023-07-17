package ru.vzotov.jqpdf.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BitsTest {

    @Test
    void get() {
        Bits bits = Bits.valueOf(new byte[]{
                (byte) 0x0f,
                (byte) 0xf0,
                (byte) 0x0a,
                (byte) 0xa0,
                (byte) 0xb0,
                (byte) 0x0b,
                (byte) 0x90,
                (byte) 0x09,
                (byte) 0x08,
                (byte) 0x80,
                (byte) 0x07,
                (byte) 0x70,
        });

        Assertions.assertEquals(0x00, bits.get(0, 1));
        Assertions.assertEquals(0x01, bits.get(4, 5));

        Assertions.assertEquals(0x0F, bits.get(0, 8));
        Assertions.assertEquals(0xF0, bits.get(8, 16));
        Assertions.assertEquals(0x0ff00aa0b00b9009L, bits.get(0, 64));
        Assertions.assertEquals(0xb00b900908800770L, bits.get(32, 64+32));
//        Assertions.assertEquals(0x09900bb0a00af00fL, bits.get(0, 64));

    }
}
