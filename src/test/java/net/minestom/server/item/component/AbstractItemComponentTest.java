package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractItemComponentTest<T> {

    protected abstract @NotNull DataComponent<T> component();

    protected abstract @NotNull List<Map.Entry<String, T>> directReadWriteEntries();

    private @NotNull Stream<Arguments> directReadWriteMethodSource() {
        return directReadWriteEntries().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directReadWriteMethodSource")
    public void directReadWriteNbt(String testName, @NotNull T entry) {
        assumeTrue(component().isSerialized());

        var written1 = component().write(entry);

        var read = component().read(written1);
        assertEquals(entry, read);

        var written2 = component().write(read);
        assertEquals(written1, written2);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directReadWriteMethodSource")
    public void directReadWriteNetwork(String testName, @NotNull T entry) {
        assumeTrue(component().isSynced());

        var written1 = NetworkBuffer.makeArray(b -> component().write(b, entry));

        var read = component().read(new NetworkBuffer(ByteBuffer.wrap(written1)));
        assertEquals(entry, read);

        var written2 = NetworkBuffer.makeArray(b -> component().write(b, entry));
        assertArrayEquals(written1, written2);
    }
}
