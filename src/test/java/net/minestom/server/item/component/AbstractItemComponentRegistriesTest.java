package net.minestom.server.item.component;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RegistriesTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractItemComponentRegistriesTest<T> {

    protected abstract DataComponent<T> component();

    protected abstract List<Map.Entry<String, T>> directReadWriteEntries();

    private Stream<Arguments> directReadWriteMethodSource() {
        return directReadWriteEntries().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directReadWriteMethodSource")
    public void directReadWriteTest(String testName, T entry, Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.NBT, registries);
        if (component().isSerialized()) {
            var written1 = assertOk(component().encode(coder, entry));

            var read = assertOk(component().decode(coder, written1));
            assertEquals(entry, read);

            var written2 = assertOk(component().encode(coder, read));
            assertEquals(written1, written2);
        }

        if (component().isSynced()) {
            var written1 = NetworkBuffer.makeArray(b -> component().write(b, entry), registries);

            var buffer = NetworkBuffer.wrap(written1, 0, written1.length, registries);
            var read = component().read(buffer);
            assertEquals(entry, read);

            var written2 = NetworkBuffer.makeArray(b -> component().write(b, entry), registries);
            assertArrayEquals(written1, written2);
        }
    }
}
