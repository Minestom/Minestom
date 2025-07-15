package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

public class SeededContainerLootTest extends AbstractItemComponentTest<SeededContainerLoot> {

    @Override
    protected DataComponent<SeededContainerLoot> component() {
        return DataComponents.CONTAINER_LOOT;
    }

    @Override
    protected List<Map.Entry<String, SeededContainerLoot>> directReadWriteEntries() {
        return List.of(
                Map.entry("instance", new SeededContainerLoot("loot_table", 1234567890L))
        );
    }

}
