package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

public class MapPostProcessingTest extends AbstractItemComponentTest<MapPostProcessing> {
    @Override
    protected DataComponent<MapPostProcessing> component() {
        return DataComponents.MAP_POST_PROCESSING;
    }

    @Override
    protected List<Map.Entry<String, MapPostProcessing>> directReadWriteEntries() {
        return List.of(
                Map.entry("lock", MapPostProcessing.LOCK),
                Map.entry("scale", MapPostProcessing.SCALE)
        );
    }
}
