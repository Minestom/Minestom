package net.minestom.server.codec;

import com.google.gson.JsonParser;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertError;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructCodecTest {

    @Test
    void emptyObject() {
        value record Empty() {
        }

        var codec = StructCodec.struct(Empty::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new Empty(), assertOk(result));
    }

    @Test
    void singleField() {
        value record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{name: \"test\"}"));
        assertEquals(new TheObject("test"), assertOk(result));
    }

    @Test
    void singleFieldMissing() {
        value record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING, TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertError("name: No such key: name", result);
    }

    @Test
    void singleFieldOptionalMissing() {
        value record TheObject(@Nullable String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional(), TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new TheObject(null), assertOk(result));
    }

    @Test
    void singleFieldOptionalMissingDefault() {
        value record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional("defaultValue"), TheObject::name,
                TheObject::new);
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{}"));
        assertEquals(new TheObject("defaultValue"), assertOk(result));
    }

    @Test
    void singleFieldOptionalIncorrectTypeButNotMissing() {
        value record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional(), TheObject::name,
                TheObject::new
        );
        var result = codec.decode(TranscoderNbtImpl.INSTANCE, snbt("{\"name\": 2}"));
        assertError("name: Not a string: IntBinaryTagImpl[value=2]", result);
    }

    @Test
    void singleFieldOptionalExplicitJsonNull() {
        value record TheObject(@Nullable String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional(), TheObject::name,
                TheObject::new);
        var json = JsonParser.parseString("{\"name\": null}");
        assertEquals(new TheObject(null), assertOk(codec.decode(Transcoder.JSON, json)));
    }

    @Test
    void singleFieldOptionalExplicitJsonNullWithDefault() {
        value record TheObject(String name) {
        }

        var codec = StructCodec.struct(
                "name", Codec.STRING.optional("defaultValue"), TheObject::name,
                TheObject::new);
        var json = JsonParser.parseString("{\"name\": null}");
        assertEquals(new TheObject("defaultValue"), assertOk(codec.decode(Transcoder.JSON, json)));
    }

    @Test
    void inlineField() {
        value record InnerObject(String value) {
        }
        value record TheObject(String name, InnerObject inner) {
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
        value record InnerObject(String value) {
        }
        value record TheObject(String name, InnerObject inner) {
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

    private static BinaryTag snbt(String snbt) {
        return assertDoesNotThrow(() -> MinestomAdventure.tagStringIO().asTag(snbt));
    }


}
