package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;

import java.util.List;
import java.util.Map;

public class LodestoneTrackerTest extends AbstractItemComponentTest<LodestoneTracker> {

    @Override
    protected DataComponent<LodestoneTracker> component() {
        return DataComponents.LODESTONE_TRACKER;
    }

    @Override
    protected List<Map.Entry<String, LodestoneTracker>> directReadWriteEntries() {
        return List.of(
            Map.entry("tracked", new LodestoneTracker("minecraft:overworld", Vec.ZERO, true)),
            Map.entry("not tracked", new LodestoneTracker("minecraft:overworld", new Vec(1, 2, 3), false))
        );
    }

}
