package net.minestom.server.codec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

public final class CodecTest {

    private static List<Arguments> optionalResults() {
        return nonDestructiveTranscoders().stream().flatMap(transcoder -> Stream.of(
                Arguments.of(transcoder, Codec.BOOLEAN, Boolean.FALSE),
                Arguments.of(transcoder, Codec.INT, 5125),
                Arguments.of(transcoder, Codec.LONG, 5125123L),
                Arguments.of(transcoder, Codec.FLOAT, 0.62143f),
                Arguments.of(transcoder, Codec.DOUBLE, 15.2d),
                Arguments.of(transcoder, Codec.BYTE, (byte) 7),
                Arguments.of(transcoder, Codec.SHORT, (short) 0),
                Arguments.of(transcoder, Codec.STRING, "scary")
        )).toList();
    }

    private static List<Transcoder<?>> nonDestructiveTranscoders() {
        return List.of(Transcoder.NBT, Transcoder.JSON, Transcoder.JAVA);
    }

    @ParameterizedTest
    @MethodSource("optionalResults")
    public <D, T> void testOptionalNullDecode(Transcoder<D> transcoder, Codec<T> codec, T expected) {
        var optionalCodec = codec.optional(expected);
        var result = optionalCodec.decode(transcoder, transcoder.createNull());
        CodecAssertions.assertOk(result);
        Assertions.assertEquals(expected, result.orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("optionalResults")
    public <D, T> void testOptionalEncodeDecodeValue(Transcoder<D> transcoder, Codec<T> codec, T expected) {
        var optionalCodec = codec.optional(expected);
        var encodeResult = optionalCodec.encode(transcoder, null);
        var result = optionalCodec.decode(transcoder, encodeResult.orElseThrow());
        CodecAssertions.assertOk(result);
        Assertions.assertEquals(expected, result.orElseThrow());
    }

    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void testListUnmodifiable(Transcoder<D> transcoder) {
        List<String> testList = Arrays.asList("Hey", "How", "Are", "You");
        var codec = Codec.STRING.list();
        var encoded = codec.encode(transcoder, testList);
        CodecAssertions.assertOk(encoded);
        var decoded = codec.decode(transcoder, encoded.orElseThrow());
        CodecAssertions.assertOk(decoded);
        var decodedObject = decoded.orElseThrow();
        Assertions.assertEquals(testList, decodedObject);
        Assertions.assertDoesNotThrow(() -> testList.set(0, "Test"));
        Assertions.assertNotEquals(testList, decodedObject);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> decodedObject.set(0, "Test"));
    }

    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void testSetUnmodifiable(Transcoder<D> transcoder) {
        var testSet = new HashSet<>(Set.of("Hey", "How", "Are", "You"));
        var codec = Codec.STRING.set();
        var encoded = codec.encode(transcoder, testSet);
        CodecAssertions.assertOk(encoded);
        var decoded = codec.decode(transcoder, encoded.orElseThrow());
        CodecAssertions.assertOk(decoded);
        var decodedObject = decoded.orElseThrow();
        Assertions.assertEquals(testSet, decodedObject);
        Assertions.assertDoesNotThrow(() -> testSet.remove("Hey"));
        Assertions.assertNotEquals(testSet, decodedObject);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> decodedObject.remove("Hey"));
    }

    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @MethodSource("nonDestructiveTranscoders")
    <D> void testMapUnmodifiable(Transcoder<D> transcoder) {
        var testSet = new HashMap<>(Map.of("Hey", "How", "Are", "You"));
        var codec = Codec.STRING.mapValue(Codec.STRING);
        var encoded = codec.encode(transcoder, testSet);
        CodecAssertions.assertOk(encoded);
        var decoded = codec.decode(transcoder, encoded.orElseThrow());
        CodecAssertions.assertOk(decoded);
        var decodedObject = decoded.orElseThrow();
        Assertions.assertEquals(testSet, decodedObject);
        Assertions.assertDoesNotThrow(() -> testSet.remove("Hey"));
        Assertions.assertNotEquals(testSet, decodedObject);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> decodedObject.remove("Hey"));
    }
}
