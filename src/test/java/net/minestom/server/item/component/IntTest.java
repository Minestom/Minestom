package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class IntTest extends AbstractItemComponentTest<Integer> {
    // This is not a test, but it creates a compile error if the component type is changed away from Integer,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Integer>> INT_COMPONENTS = List.of(
           ItemComponent.MAX_STACK_SIZE,
           ItemComponent.MAX_DAMAGE,
           ItemComponent.DAMAGE,
           ItemComponent.CUSTOM_MODEL_DATA,
           ItemComponent.REPAIR_COST,
           ItemComponent.MAP_ID,
           ItemComponent.OMINOUS_BOTTLE_AMPLIFIER
    );

    @Override
    protected @NotNull DataComponent<Integer> component() {
        return INT_COMPONENTS.getFirst();
    }

    @Override
    protected @NotNull List<Map.Entry<String, Integer>> directReadWriteEntries() {
        return List.of(
                entry("instance", 2)
        );
    }
}
