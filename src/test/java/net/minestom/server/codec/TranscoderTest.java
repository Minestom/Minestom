package net.minestom.server.codec;

import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TranscoderTest {

    private static List<Transcoder<?>> nonDestructiveTranscoders() {
        return List.of(Transcoder.NBT, Transcoder.JSON, Transcoder.JAVA);
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void createNumberDispatchesOnRuntimeType(Transcoder<D> transcoder) {
        assertEquals((byte) 7, assertOk(transcoder.getNumber(transcoder.createNumber((byte) 7))).byteValue());
        assertEquals((short) 9, assertOk(transcoder.getNumber(transcoder.createNumber((short) 9))).shortValue());
        assertEquals(42, assertOk(transcoder.getNumber(transcoder.createNumber(42))).intValue());
        assertEquals(42L, assertOk(transcoder.getNumber(transcoder.createNumber(42L))).longValue());
        assertEquals(1.5f, assertOk(transcoder.getNumber(transcoder.createNumber(1.5f))).floatValue());
        assertEquals(1.5d, assertOk(transcoder.getNumber(transcoder.createNumber(1.5d))).doubleValue());
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void getNumberReadsPrimitiveCreators(Transcoder<D> transcoder) {
        assertEquals(5, assertOk(transcoder.getNumber(transcoder.createInt(5))).intValue());
        assertEquals(5L, assertOk(transcoder.getNumber(transcoder.createLong(5L))).longValue());
        assertEquals((byte) 5, assertOk(transcoder.getNumber(transcoder.createByte((byte) 5))).byteValue());
        assertEquals((short) 5, assertOk(transcoder.getNumber(transcoder.createShort((short) 5))).shortValue());
        assertEquals(2.5f, assertOk(transcoder.getNumber(transcoder.createFloat(2.5f))).floatValue());
        assertEquals(2.5d, assertOk(transcoder.getNumber(transcoder.createDouble(2.5d))).doubleValue());
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void getNumberRejectsNonNumber(Transcoder<D> transcoder) {
        assertInstanceOf(Result.Error.class, transcoder.getNumber(transcoder.createString("not a number")));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void byteArrayCoercesAndNarrowsFromIntList(Transcoder<D> transcoder) {
        final D list = transcoder.createList(3)
                .add(transcoder.createInt(1))
                .add(transcoder.createInt(2))
                .add(transcoder.createInt(300))
                .build();
        assertArrayEquals(new byte[]{1, 2, (byte) 300}, assertOk(transcoder.getByteArray(list)));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void intArrayCoercesAndNarrowsFromLongList(Transcoder<D> transcoder) {
        final D list = transcoder.createList(2)
                .add(transcoder.createLong(1L))
                .add(transcoder.createLong(0x1_0000_0001L))
                .build();
        assertArrayEquals(new int[]{1, 1}, assertOk(transcoder.getIntArray(list)));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void longArrayCoercesFromIntList(Transcoder<D> transcoder) {
        final D list = transcoder.createList(2)
                .add(transcoder.createInt(1))
                .add(transcoder.createInt(-5))
                .build();
        assertArrayEquals(new long[]{1L, -5L}, assertOk(transcoder.getLongArray(list)));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void intArrayRejectsNonNumberElement(Transcoder<D> transcoder) {
        final D list = transcoder.createList(2)
                .add(transcoder.createInt(1))
                .add(transcoder.createString("nope"))
                .build();
        assertInstanceOf(Result.Error.class, transcoder.getIntArray(list));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void byteArrayRoundTrip(Transcoder<D> transcoder) {
        final byte[] data = {1, -5, 127, 0};
        final D encoded = assertOk(Codec.BYTE_ARRAY.encode(transcoder, data));
        assertArrayEquals(data, assertOk(Codec.BYTE_ARRAY.decode(transcoder, encoded)));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void intArrayRoundTrip(Transcoder<D> transcoder) {
        final int[] data = {1, -5, 1000, Integer.MAX_VALUE};
        final D encoded = assertOk(Codec.INT_ARRAY.encode(transcoder, data));
        assertArrayEquals(data, assertOk(Codec.INT_ARRAY.decode(transcoder, encoded)));
    }

    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void longArrayRoundTrip(Transcoder<D> transcoder) {
        final long[] data = {1L, -5L, Long.MAX_VALUE};
        final D encoded = assertOk(Codec.LONG_ARRAY.encode(transcoder, data));
        assertArrayEquals(data, assertOk(Codec.LONG_ARRAY.decode(transcoder, encoded)));
    }

    @Test
    void nbtReadsNativeArrayTags() {
        assertArrayEquals(new byte[]{1, 2, 3},
                assertOk(Transcoder.NBT.getByteArray(ByteArrayBinaryTag.byteArrayBinaryTag((byte) 1, (byte) 2, (byte) 3))));
        assertArrayEquals(new int[]{1, 2, 3},
                assertOk(Transcoder.NBT.getIntArray(IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3))));
        assertArrayEquals(new long[]{1L, 2L, 3L},
                assertOk(Transcoder.NBT.getLongArray(LongArrayBinaryTag.longArrayBinaryTag(1L, 2L, 3L))));
    }

    @Test
    void nbtEncodesArraysToNativeTags() {
        assertInstanceOf(ByteArrayBinaryTag.class, assertOk(Codec.BYTE_ARRAY.encode(Transcoder.NBT, new byte[]{1, 2})));
        assertInstanceOf(IntArrayBinaryTag.class, assertOk(Codec.INT_ARRAY.encode(Transcoder.NBT, new int[]{1, 2})));
        assertInstanceOf(LongArrayBinaryTag.class, assertOk(Codec.LONG_ARRAY.encode(Transcoder.NBT, new long[]{1, 2})));
    }

    @Test
    void nbtIntArrayFallsBackToListTag() {
        final ListBinaryTag list = ListBinaryTag.builder()
                .add(IntBinaryTag.intBinaryTag(1))
                .add(IntBinaryTag.intBinaryTag(2))
                .add(IntBinaryTag.intBinaryTag(3))
                .build();
        assertArrayEquals(new int[]{1, 2, 3}, assertOk(Transcoder.NBT.getIntArray(list)));
    }

    @Test
    void nbtLongArrayFallsBackToListTag() {
        final ListBinaryTag list = ListBinaryTag.builder()
                .add(LongBinaryTag.longBinaryTag(1L))
                .add(LongBinaryTag.longBinaryTag(2L))
                .build();
        assertArrayEquals(new long[]{1L, 2L}, assertOk(Transcoder.NBT.getLongArray(list)));
    }
}
