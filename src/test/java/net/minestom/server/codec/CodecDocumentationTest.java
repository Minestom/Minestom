package net.minestom.server.codec;

import com.google.gson.JsonElement;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class CodecDocumentationTest {


    @ParameterizedTest(name = "package-info.java example: null={0}")
    @ValueSource(booleans = {true, false})
    public void testPackageInfoExample(boolean nullName) {
        record MyType(int id, @Nullable String name) {
            static final StructCodec<MyType> CODEC = StructCodec.struct(
                    "id", Codec.INT, MyType::id,
                    "name", Codec.STRING.optional(), MyType::name,
                    MyType::new
            );
        }

        MyType value = new MyType(42, nullName ? null : "Example"); // Or use a null name for no name.
        // Encoding to JSON
        JsonElement encoded = MyType.CODEC.encode(Transcoder.JSON, value).orElseThrow();
        // Decoding from JSON
        MyType decoded = MyType.CODEC.decode(Transcoder.JSON, encoded).orElseThrow();

        Assertions.assertEquals(value, decoded);
    }


    @ParameterizedTest(name = "StructCodec example: null={0}")
    @ValueSource(booleans = {true, false})
    public void testStructCodecExample(boolean nullName) {
        record MyObject(double coolnessFactor, @Nullable String of) {
            static final StructCodec<MyObject> CODEC = StructCodec.struct(
                    "id", Codec.DOUBLE, MyObject::coolnessFactor,
                    "name", Codec.STRING.optional(), MyObject::of,
                    MyObject::new
            );

            public MyObject {
                coolnessFactor = Math.clamp(coolnessFactor, 0.0, 2.0); // Too powerful
            }
        }

        MyObject value = new MyObject(7.8d, nullName ? null : "me"); // Or use a null name for no name.
        // Encoding to JSON
        JsonElement encoded = MyObject.CODEC.encode(Transcoder.JSON, value).orElseThrow();
        // Decoding from JSON
        MyObject decoded = MyObject.CODEC.decode(Transcoder.JSON, encoded).orElseThrow();

        Assertions.assertEquals(value, decoded);
    }

    @Test
    public void testEncoderExample() {
        record Name(String imTheBoss) { }
        Encoder<Name> encoder = new Encoder<>() {
            @Override
            public <D> Result<D> encode(Transcoder<D> coder, @Nullable Name value) {
                if (value == null) return new Result.Error<>("null");
                return new Result.Ok<>(coder.createString(value.imTheBoss()));
            }
        };
        Result<BinaryTag> result = encoder.encode(Transcoder.NBT, new Name("me"));
        Result<BinaryTag> errorResult = encoder.encode(Transcoder.NBT, null);
        Assertions.assertEquals(StringBinaryTag.stringBinaryTag("me"), result.orElseThrow());
        CodecAssertions.assertError("null", errorResult);
    }

    @Test
    public void testDecodingExample() {
        record Name(String imTheBoss) { }
        Decoder<Name> decoder = new Decoder<>() {
            @Override
            public <D> Result<Name> decode(Transcoder<D> coder, D value) {
                return coder.getString(value).mapResult(Name::new);
            }
        };
        Result<Name> result = decoder.decode(Transcoder.NBT, StringBinaryTag.stringBinaryTag("me"));
        Result<Name> errorResult = decoder.decode(Transcoder.NBT, EndBinaryTag.endBinaryTag());
        Assertions.assertEquals(new Name("me"), result.orElseThrow());
        Assertions.assertInstanceOf(Result.Error.class, errorResult);
    }
}
