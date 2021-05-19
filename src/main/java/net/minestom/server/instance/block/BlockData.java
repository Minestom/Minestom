package net.minestom.server.instance.block;

import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockData {
    // Block properties
    double getExplosionResistance();
    @Nullable Material getCorrespondingItem();
    double getFriction();
    double getSpeedFactor();
    double getJumpFactor();
    boolean isBlockEntity();

    // State properties
    double getHardness();
    int getLightEmission();
    boolean isOccluding();
    String getPushReaction(); // TODO: Dedicated object?
    boolean isBlockingMotion();
    boolean isFlammable();
    boolean isAir();
    boolean isLiquid();
    boolean isReplaceable();
    boolean isSolid();
    boolean isSolidBlocking();
    @NotNull MapColor getMapColor();
    String getBoundingBox(); // TODO: Dedicated object?
}
