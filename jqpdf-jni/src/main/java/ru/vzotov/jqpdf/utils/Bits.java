package ru.vzotov.jqpdf.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Bits {

    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private static final int BIT_INDEX_MASK = BITS_PER_WORD - 1;
    private static final long WORD_MASK = 0xffffffffffffffffL;

    private final long[] words;

    private final int wordsCount;

    private Bits(long[] words) {
        this.words = words;
        this.wordsCount = words.length;
    }

    public int length() {
        return BITS_PER_WORD * wordsCount;
    }

    public static Bits valueOf(byte[] bytes) {
        return Bits.valueOf(ByteBuffer.wrap(bytes));
    }

    public static Bits valueOf(ByteBuffer bytes) {
        bytes = bytes.slice().order(ByteOrder.BIG_ENDIAN);

        int n;
        for (n = bytes.remaining(); n > 0 && bytes.get(n - 1) == 0; n--) {
        }

        long[] words = new long[(n + 7) / 8];
        bytes.limit(n);

        int i = 0;
        while (bytes.remaining() >= 8) {
            words[i++] = bytes.getLong();
        }
        for (int remaining = bytes.remaining(), j = 1; j <= remaining; j++) {
            words[i] |= (bytes.get() & 0xffL) << (BITS_PER_WORD - 8 * j);
        }
        return new Bits(words);

    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    public boolean get(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        return (wordIndex < wordsCount)
                && ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    public long get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        int len = length();

        // If no set bits in range return empty bitset
        if (len <= fromIndex || fromIndex == toIndex)
            return 0;

        // An optimization
        if (toIndex > len)
            toIndex = len;

        long result = 0L;
        int sourceIndex = wordIndex(fromIndex);

        if (((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)) {
            final long lastWordMask = WORD_MASK << -toIndex;
            /* straddles source words */
            result = ((words[sourceIndex] & (WORD_MASK >>> fromIndex)) << toIndex) | (words[sourceIndex + 1] & lastWordMask) >>> -toIndex;
        } else {
            long firstWordMask = (WORD_MASK << fromIndex) >>> fromIndex;
            result = (words[sourceIndex] & firstWordMask) >>> BITS_PER_WORD - toIndex;
        }

        return result;
    }

    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        if (toIndex < 0)
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        if (fromIndex > toIndex)
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex +
                    " > toIndex: " + toIndex);
    }

}
