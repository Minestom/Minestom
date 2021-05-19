package net.minestom.server.instance.block;

import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlockDataImpl implements BlockData {
    private final double explosionResistance;
    private final @NotNull Material item;
    private final double friction;
    private final double speedFactor;
    private final double jumpFactor;
    private final double hardness;
    private final boolean blockEntity;

    private final int lightEmission;
    private final boolean occluding;
    private final String pushReaction; // TODO: Dedicated object?
    private final boolean blockingMotion;
    private final boolean flammable;
    private final boolean air;
    private final boolean liquid;
    private final boolean replaceable;
    private final boolean solid;
    private final boolean solidBlocking;
    private final @NotNull MapColor mapColor;
    private final String boundingBox; // TODO: Dedicated object?


    BlockDataImpl(
            double explosionResistance,
            @NotNull Material item,
            double friction,
            double speedFactor,
            double jumpFactor,
            boolean blockEntity,

            double hardness,
            int lightEmission,
            boolean occluding,
            String pushReaction,
            boolean blockingMotion,
            boolean flammable,
            boolean air,
            boolean liquid,
            boolean replaceable,
            boolean solid,
            boolean solidBlocking,
            @NotNull MapColor mapColor,
            String boundingBox
    ) {
        this.explosionResistance = explosionResistance;
        this.item = item;
        this.friction = friction;
        this.speedFactor = speedFactor;
        this.jumpFactor = jumpFactor;
        this.hardness = hardness;
        this.blockEntity = blockEntity;
        this.lightEmission = lightEmission;
        this.occluding = occluding;
        this.pushReaction = pushReaction;
        this.blockingMotion = blockingMotion;
        this.air = air;
        this.flammable = flammable;
        this.liquid = liquid;
        this.replaceable = replaceable;
        this.solid = solid;
        this.solidBlocking = solidBlocking;
        this.mapColor = mapColor;
        this.boundingBox = boundingBox;
    }

    @Override
    public double getExplosionResistance() {
        return explosionResistance;
    }

    @Override
    public @Nullable Material getCorrespondingItem() {
        return item;
    }

    @Override
    public double getFriction() {
        return friction;
    }

    @Override
    public double getSpeedFactor() {
        return speedFactor;
    }

    @Override
    public double getJumpFactor() {
        return jumpFactor;
    }

    @Override
    public double getHardness() {
        return hardness;
    }

    @Override
    public boolean isBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getLightEmission() {
        return lightEmission;
    }

    @Override
    public boolean isOccluding() {
        return occluding;
    }

    @Override
    public String getPushReaction() {
        return pushReaction;
    }

    @Override
    public boolean isBlockingMotion() {
        return blockingMotion;
    }

    @Override
    public boolean isFlammable() {
        return flammable;
    }

    @Override
    public boolean isAir() {
        return air;
    }

    @Override
    public boolean isLiquid() {
        return liquid;
    }

    @Override
    public boolean isReplaceable() {
        return replaceable;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public boolean isSolidBlocking() {
        return solidBlocking;
    }

    @Override
    public @NotNull MapColor getMapColor() {
        return mapColor;
    }

    @Override
    public String getBoundingBox() {
        return boundingBox;
    }
}
