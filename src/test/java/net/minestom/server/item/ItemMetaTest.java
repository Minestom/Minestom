package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.banner.BannerPattern;
import net.minestom.server.item.metadata.BannerMeta;
import net.minestom.server.item.metadata.BundleMeta;
import net.minestom.server.item.metadata.PlayerHeadMeta;
import net.minestom.server.item.metadata.ShieldMeta;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemMetaTest {
    @Test
    void defaultMeta() {
        var item = ItemStack.builder(Material.BUNDLE).build();
        assertNotNull(item.meta());
    }

    @Test
    void fromNBT() {
        var compound = NBT.Compound(Map.of("value", NBT.Int(5)));
        var item = ItemStack.builder(Material.BUNDLE).meta(compound).build();
        assertEquals(compound, item.meta().toNBT());
    }

    @Test
    void bundle() {
        var item = ItemStack.builder(Material.BUNDLE)
                .meta(BundleMeta.class, bundleMetaBuilder -> {
                    bundleMetaBuilder.addItem(ItemStack.of(Material.DIAMOND, 5));
                    bundleMetaBuilder.addItem(ItemStack.of(Material.RABBIT_FOOT, 5));
                })
                .build();
        assertEquals(2, item.meta(BundleMeta.class).getItems().size());
    }

    @Test
    void buildView() {
        var uuid = UUID.randomUUID();
        var skin = new PlayerSkin("xx", "yy");
        var meta = new PlayerHeadMeta.Builder()
                .skullOwner(uuid)
                .playerSkin(skin)
                .build();
        var item = ItemStack.builder(Material.PLAYER_HEAD)
                .meta(meta)
                .displayName(Component.text("Name"))
                .build();
        PlayerHeadMeta view = item.meta(PlayerHeadMeta.class);
        assertEquals(uuid, view.getSkullOwner());
        assertEquals(skin, view.getPlayerSkin());
    }

    // Microtus start - Add tests for the banner and shield meta
    @Test
    void banner() {
        var item = ItemStack.builder(Material.WHITE_BANNER)
                .meta(BannerMeta.class, bannerMetaBuilder -> {
                    bannerMetaBuilder.customName(Component.text("Test Banner"));
                    bannerMetaBuilder.addPattern(new BannerMeta.Pattern(DyeColor.BLUE, BannerPattern.BORDER));
                    bannerMetaBuilder.addPattern(new BannerMeta.Pattern(DyeColor.LIGHT_BLUE, BannerPattern.STRIPE_MIDDLE));
                })
                .build();
        assertEquals(Component.text("Test Banner"), item.meta(BannerMeta.class).getCustomName());
        assertEquals(2, item.meta(BannerMeta.class).getPatterns().size());
        assertEquals(DyeColor.BLUE, item.meta(BannerMeta.class).getPatterns().get(0).color());
        assertEquals(BannerPattern.STRIPE_MIDDLE, item.meta(BannerMeta.class).getPatterns().get(1).pattern());
    }

    @Test
    void shield() {
        var item = ItemStack.builder(Material.SHIELD)
                .meta(ShieldMeta.class, shieldMetaBuilder -> {
                    shieldMetaBuilder.customName(Component.text("Test Shield"));
                    shieldMetaBuilder.patterns(List.of(new BannerMeta.Pattern(DyeColor.BLACK, BannerPattern.PIGLIN)));
                    shieldMetaBuilder.baseColor(DyeColor.BLACK);
                })
                .build();
        assertEquals(Component.text("Test Shield"), item.meta(ShieldMeta.class).getCustomName());
        assertEquals(DyeColor.BLACK, item.meta(ShieldMeta.class).getBaseColor());
        assertEquals(1, item.meta(ShieldMeta.class).getPatterns().size());
        assertEquals(DyeColor.BLACK, item.meta(ShieldMeta.class).getPatterns().get(0).color());
        assertEquals(BannerPattern.PIGLIN, item.meta(ShieldMeta.class).getPatterns().get(0).pattern());
    }
    // Microtus end - Add tests for the banner and shield meta
}
