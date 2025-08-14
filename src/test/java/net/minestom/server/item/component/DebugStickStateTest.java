package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class DebugStickStateTest extends AbstractItemComponentTest<DebugStickState> {

    @Override
    protected DataComponent<DebugStickState> component() {
        return DataComponents.DEBUG_STICK_STATE;
    }

    @Override
    protected List<Map.Entry<String, DebugStickState>> directReadWriteEntries() {
        return List.of(
                entry("empty", new DebugStickState(Map.of())),
                // Note that an invalid block id is present. Minestom currently does not validate the block id or state value.
                entry("contents", new DebugStickState(Map.of("minecraft:stone_stairs", "shape", "minecraft:nothing", "abcdef")))
        );
    }

}
