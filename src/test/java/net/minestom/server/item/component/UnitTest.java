package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class UnitTest extends AbstractItemComponentTest<Unit> {
    @Override
    protected @NotNull DataComponent<Unit> component() {
        return ItemComponent.HIDE_TOOLTIP;
    }

    @Override
    protected @NotNull List<Map.Entry<String, Unit>> directReadWriteEntries() {
        return List.of(
                entry("instance", Unit.INSTANCE)
        );
    }
}
