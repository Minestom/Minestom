package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class IntTest extends AbstractItemComponentTest<Integer> {
    // This is not a test, but it creates a compile error if the component type is changed away from Integer,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Integer>> INT_COMPONENTS = List.of(
           DataComponents.MAX_STACK_SIZE,
           DataComponents.MAX_DAMAGE,
           DataComponents.DAMAGE,
           DataComponents.REPAIR_COST,
           DataComponents.MAP_ID,
           DataComponents.OMINOUS_BOTTLE_AMPLIFIER
    );

    @Override
    protected DataComponent<Integer> component() {
        return INT_COMPONENTS.getFirst();
    }

    @Override
    protected List<Map.Entry<String, Integer>> directReadWriteEntries() {
        return List.of(
                entry("instance", 2)
        );
    }
}
