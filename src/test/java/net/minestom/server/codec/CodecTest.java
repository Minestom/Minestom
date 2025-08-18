package net.minestom.server.codec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
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
        var result = optionalCodec.decode(transcoder, null);
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
}
