package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;

public class PotDecorationsTest extends AbstractItemComponentTest<PotDecorations> {
    @Override
    protected DataComponent<PotDecorations> component() {
        return DataComponents.POT_DECORATIONS;
    }

    @Override
    protected List<Map.Entry<String, PotDecorations>> directReadWriteEntries() {
        return List.of(
                Map.entry("instance", PotDecorations.EMPTY),
                Map.entry("one", new PotDecorations(Material.DIAMOND, PotDecorations.DEFAULT_ITEM, PotDecorations.DEFAULT_ITEM, PotDecorations.DEFAULT_ITEM)),
                Map.entry("two", new PotDecorations(Material.DIAMOND, Material.DIAMOND, PotDecorations.DEFAULT_ITEM, PotDecorations.DEFAULT_ITEM)),
                Map.entry("three", new PotDecorations(Material.DIAMOND, Material.DIAMOND, Material.DIAMOND, PotDecorations.DEFAULT_ITEM)),
                Map.entry("four", new PotDecorations(Material.DIAMOND, Material.DIAMOND, Material.DIAMOND, Material.DIAMOND))
        );
    }
}
