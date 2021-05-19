package net.minestom.server.instance.block;

import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockData {
    /**
     * @return The explosion resistance of the Block
     */
    double getExplosionResistance();
    @Nullable Material getCorrespondingItem();
    double getFriction();
    double getSpeedFactor();
    double getJumpFactor();
    double getHardness();
    int getLightEmission();
    boolean isOccluding();
    String getPushReaction(); // TODO: Dedicated object?
    boolean isBlockingMotion();
    boolean isFlammable();
    boolean isLiquid();
    boolean isReplaceable();
    boolean isSolid();
    boolean isSolidBlocking();
    @NotNull MapColor getMapColor();
    String getBoundingBox(); // TODO: Dedicated object?
}
