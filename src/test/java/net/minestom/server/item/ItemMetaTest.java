package net.minestom.server.item;

import net.minestom.server.item.metadata.BundleMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMetaTest {
    @Test
    public void bundle() {
        var item = ItemStack.builder(Material.BUNDLE)
                .meta(BundleMeta.class, bundleMetaBuilder -> {
                    bundleMetaBuilder.addItem(ItemStack.of(Material.DIAMOND, 5));
                    bundleMetaBuilder.addItem(ItemStack.of(Material.RABBIT_FOOT, 5));
                })
                .build();
        assertEquals(2, item.meta(BundleMeta.class).getItems().size());
    }
}
