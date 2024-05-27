package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class UnitTest extends AbstractItemComponentTest<Unit> {
    // This is not a test, but it creates a compile error if the component type is changed away from Unit,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Unit>> UNIT_COMPONENTS = List.of(
            ItemComponent.HIDE_ADDITIONAL_TOOLTIP,
            ItemComponent.HIDE_TOOLTIP,
            ItemComponent.CREATIVE_SLOT_LOCK,
            ItemComponent.INTANGIBLE_PROJECTILE,
            ItemComponent.FIRE_RESISTANT
    );

    @Override
    protected @NotNull DataComponent<Unit> component() {
        return UNIT_COMPONENTS.getFirst();
    }

    @Override
    protected @NotNull List<Map.Entry<String, Unit>> directReadWriteEntries() {
        return List.of(
                entry("instance", Unit.INSTANCE)
        );
    }
}
