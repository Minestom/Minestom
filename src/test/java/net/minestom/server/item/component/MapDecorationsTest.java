package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

public class MapDecorationsTest extends AbstractItemComponentTest<MapDecorations> {

    @Override
    protected DataComponent<MapDecorations> component() {
        return DataComponents.MAP_DECORATIONS;
    }

    @Override
    protected List<Map.Entry<String, MapDecorations>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", new MapDecorations(Map.of())),
                Map.entry("single", new MapDecorations(Map.of("id", new MapDecorations.Entry("type", 1.0, 2.0, 3)))),
                Map.entry("multiple", new MapDecorations(Map.of(
                        "id1", new MapDecorations.Entry("type1", 1.0, 2.0, 3),
                        "id2", new MapDecorations.Entry("type2", 4.0, 5.0, 6)
                )))
        );
    }

}
