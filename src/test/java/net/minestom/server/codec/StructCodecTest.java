package net.minestom.server.codec;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertError;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.*;

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
        assertError("name: No such key: name", result);
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

    @Test
    void singleFieldOptionalIncorrectTypeButNotMissing() {
        record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional(), TheObject::name,
                TheObject::new
        );
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{\"name\": 2}"));
        assertError("name: Not a string: BinaryTagType[IntBinaryTag 3 (numeric)]{value=2}", result);
    }

    @Test
    void inlineField() {
        record InnerObject(String value) {
        }
        record TheObject(String name, InnerObject inner) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                StructCodec.INLINE, StructCodec.struct(
                        "value", Codec.STRING, InnerObject::value,
                        InnerObject::new
                ), TheObject::inner,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{name: \"test\", value: \"innerValue\"}"));
        assertEquals(new TheObject("test", new InnerObject("innerValue")), assertOk(result));

        var encodeResult = codec.encode(TranscoderNbtImpl.INSTANCE, new TheObject("test", new InnerObject("innerValue")));
        assertEquals(snbt("{name: \"test\", value: \"innerValue\"}"), assertOk(encodeResult));
    }

    @Test
    void inlineFieldEmpty() {
        record InnerObject(String value) {
        }
        record TheObject(String name, InnerObject inner) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                StructCodec.INLINE, StructCodec.struct(
                        "value", Codec.STRING, InnerObject::value,
                        InnerObject::new
                ), TheObject::inner,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{name: \"test\", value: \"innerValue\"}"));
        assertEquals(new TheObject("test", new InnerObject("innerValue")), assertOk(result));

        var encodeResult = codec.encode(TranscoderNbtImpl.INSTANCE, new TheObject("test", new InnerObject("innerValue")));
        assertEquals(snbt("{name: \"test\", value: \"innerValue\"}"), assertOk(encodeResult));
    }

    private BinaryTag snbt(String snbt) {
        return assertDoesNotThrow(() -> MinestomAdventure.tagStringIO().asTag(snbt));
    }


}
