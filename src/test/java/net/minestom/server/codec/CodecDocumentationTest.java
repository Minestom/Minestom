package net.minestom.server.codec;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class CodecDocumentationTest {


    @ParameterizedTest(name = "package-info.java example: null={0}")
    @ValueSource(booleans = {true, false})
    public void testPackageInfoExample(boolean nullName) {
        record MyType(int id, @Nullable String name) {
            static final Codec<MyType> CODEC = StructCodec.struct(
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
}
