package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class DyedItemColorTest extends AbstractItemComponentTest<DyedItemColor> {

    @Override
    protected @NotNull DataComponent<DyedItemColor> component() {
        return ItemComponent.DYED_COLOR;
    }

    @Override
    protected @NotNull List<Map.Entry<String, DyedItemColor>> directReadWriteEntries() {
        return List.of(
                entry("default leather", DyedItemColor.LEATHER),
                entry("no tooltip", new DyedItemColor(0xCAFEBB, false))
        );
    }
}
