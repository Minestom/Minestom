package net.minestom.server.item.component;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractItemComponentTest<T> {

    protected abstract @NotNull DataComponent<T> component();

    protected abstract @NotNull List<Map.Entry<String, T>> directReadWriteEntries();

    private @NotNull Stream<Arguments> directReadWriteMethodSource() {
        return directReadWriteEntries().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directReadWriteMethodSource")
    public void directReadWriteTest(String testName, @NotNull T entry, Env env) {
        var context = new BinaryTagSerializer.ContextWithRegistries(env.process());
        if (component().isSerialized()) {
            var written1 = component().write(context, entry);

            var read = component().read(context, written1);
            assertEquals(entry, read);

            var written2 = component().write(context, read);
            assertEquals(written1, written2);
        }

        if (component().isSynced()) {
            var written1 = NetworkBuffer.makeArray(b -> component().write(b, entry), MinecraftServer.process());

            var buffer = NetworkBuffer.wrap(written1, 0, written1.length, MinecraftServer.process());
            var read = component().read(buffer);
            assertEquals(entry, read);

            var written2 = NetworkBuffer.makeArray(b -> component().write(b, entry), MinecraftServer.process());
            assertArrayEquals(written1, written2);
        }
    }
}
