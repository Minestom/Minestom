package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class BoolTest extends AbstractItemComponentTest<Boolean> {
    // This is not a test, but it creates a compile error if the component type is changed away from boolean,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Boolean>> SHARED_COMPONENTS = List.of(
           DataComponents.ENCHANTMENT_GLINT_OVERRIDE
    );

    @Override
    protected DataComponent<Boolean> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected List<Map.Entry<String, Boolean>> directReadWriteEntries() {
        return List.of(
                entry("true", true),
                entry("false", false)
        );
    }
}
