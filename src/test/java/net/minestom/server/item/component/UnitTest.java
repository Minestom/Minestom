package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.fail;

public class UnitTest extends AbstractItemComponentTest<Unit> {
    // This is not a test, but it creates a compile error if the component type is changed away from Unit,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<Unit>> UNIT_COMPONENTS = List.of(
            ItemComponent.HIDE_ADDITIONAL_TOOLTIP,
            ItemComponent.HIDE_TOOLTIP,
            ItemComponent.CREATIVE_SLOT_LOCK,
            ItemComponent.INTANGIBLE_PROJECTILE,
            ItemComponent.GLIDER
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

    @Test
    public void ensureUnitComponentsPresent() {
        var fails = new ArrayList<String>();
        for (var component : ItemComponent.values()) {
            if (!component.isSynced()) continue;

            // Try to write as a Unit and if it fails we can ignore that type
            try {
                //noinspection unchecked
                ((DataComponent<Unit>) component).write(NetworkBuffer.resizableBuffer(), Unit.INSTANCE);
            } catch (ClassCastException ignored) {
                continue;
            }

            if (!UNIT_COMPONENTS.contains(component)) {
                fails.add(component.name());
            }
        }

        if (!fails.isEmpty()) {
            fail("Some components are not included in UnitTest: " + fails);
        }
    }
}
