package net.minestom.server.codec;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertError;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructCodecTest {

    @Test
    void emptyObject() {
        record Empty() {
        }

        var codec = StructCodec.struct(Empty::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new Empty(), assertOk(result));
    }

    @Test
    void singleField() {
        record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{name: \"test\"}"));
        assertEquals(new TheObject("test"), assertOk(result));
    }

    @Test
    void singleFieldMissing() {
        record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertError("No such key: name", result);
    }

    @Test
    void singleFieldOptionalMissing() {
        record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional(), TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new TheObject(null), assertOk(result));
    }

    @Test
    void singleFieldOptionalMissingDefault() {
        record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional("defaultValue"), TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new TheObject("defaultValue"), assertOk(result));
    }

    private @NotNull BinaryTag snbt(@NotNull String snbt) {
        return assertDoesNotThrow(() -> TagStringIOExt.readTag(snbt));
    }


}
