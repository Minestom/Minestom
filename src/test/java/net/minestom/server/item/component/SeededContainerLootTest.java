package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SeededContainerLootTest extends AbstractItemComponentTest<SeededContainerLoot> {

    @Override
    protected @NotNull DataComponent<SeededContainerLoot> component() {
        return ItemComponent.CONTAINER_LOOT;
    }

    @Override
    protected @NotNull List<Map.Entry<String, SeededContainerLoot>> directReadWriteEntries() {
        return List.of(
                Map.entry("instance", new SeededContainerLoot("loot_table", 1234567890L))
        );
    }

}
