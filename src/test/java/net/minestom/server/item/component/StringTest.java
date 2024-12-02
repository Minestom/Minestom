package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class StringTest extends AbstractItemComponentTest<String> {
    // This is not a test, but it creates a compile error if the component type is changed away,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<String>> SHARED_COMPONENTS = List.of(
           ItemComponent.INSTRUMENT,
           ItemComponent.NOTE_BLOCK_SOUND
    );

    @Override
    protected @NotNull DataComponent<String> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected @NotNull List<Map.Entry<String, String>> directReadWriteEntries() {
        return List.of(
                entry("instance", "hello, world")
        );
    }
}
