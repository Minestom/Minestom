package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;

public class PotDecorationsTest extends AbstractItemComponentRegistriesTest<PotDecorations> {
    private static final ItemStack DIAMOND = ItemStack.of(Material.DIAMOND);

    @Override
    protected DataComponent<PotDecorations> component() {
        return DataComponents.POT_DECORATIONS;
    }

    @Override
    protected List<Map.Entry<String, PotDecorations>> directReadWriteEntries() {
        return List.of(
                Map.entry("instance", PotDecorations.EMPTY),
                Map.entry("one", new PotDecorations(DIAMOND, null, null, null)),
                Map.entry("two", new PotDecorations(DIAMOND, DIAMOND, null, null)),
                Map.entry("three", new PotDecorations(DIAMOND, DIAMOND, DIAMOND, null)),
                Map.entry("four", new PotDecorations(DIAMOND))
        );
    }
}
