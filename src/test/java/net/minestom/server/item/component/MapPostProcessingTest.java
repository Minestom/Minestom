package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MapPostProcessingTest extends AbstractItemComponentTest<MapPostProcessing> {
    @Override
    protected @NotNull DataComponent<MapPostProcessing> component() {
        return ItemComponent.MAP_POST_PROCESSING;
    }

    @Override
    protected @NotNull List<Map.Entry<String, MapPostProcessing>> directReadWriteEntries() {
        return List.of(
                Map.entry("lock", MapPostProcessing.LOCK),
                Map.entry("scale", MapPostProcessing.SCALE)
        );
    }
}
