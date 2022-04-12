package net.minestom.server.item;

import net.minestom.server.item.metadata.BundleMeta;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemMetaTest {
    @Test
    public void defaultMeta() {
        var item = ItemStack.builder(Material.BUNDLE).build();
        assertNotNull(item.meta());
    }

    @Test
    public void fromNBT() {
        var compound = NBT.Compound(Map.of("value", NBT.Int(5)));
        var item = ItemStack.builder(Material.BUNDLE).meta(compound).build();
        assertEquals(compound, item.meta().toNBT());
    }

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
