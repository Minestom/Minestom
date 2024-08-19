package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class LodestoneTrackerTest extends AbstractItemComponentTest<LodestoneTracker> {

    @Override
    protected @NotNull DataComponent<LodestoneTracker> component() {
        return ItemComponent.LODESTONE_TRACKER;
    }

    @Override
    protected @NotNull List<Map.Entry<String, LodestoneTracker>> directReadWriteEntries() {
        return List.of(
            Map.entry("tracked", new LodestoneTracker("minecraft:overworld", Vec.ZERO, true)),
            Map.entry("not tracked", new LodestoneTracker("minecraft:overworld", new Vec(1, 2, 3), false))
        );
    }

}
