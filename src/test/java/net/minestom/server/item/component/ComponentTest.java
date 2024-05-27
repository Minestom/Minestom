package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ComponentTest extends AbstractItemComponentTest<Component> {
    // This is not a test, but it creates a compile error if the component type is changed away from Component,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Component>> SHARED_COMPONENTS = List.of(
            ItemComponent.CUSTOM_NAME,
            ItemComponent.ITEM_NAME
    );

    @Override
    protected @NotNull DataComponent<Component> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected @NotNull List<Map.Entry<String, Component>> directReadWriteEntries() {
        // Component serialization is well tested elsewhere, this is just a sanity check really.
        return List.of(
                Map.entry("empty component", Component.empty()),
                Map.entry("text component", Component.text("Hello, world!"))
        );
    }
}
