package net.minestom.server.raw_data;

public final class RawBlockStateData {
    public final double destroySpeed;
    public final int lightEmission;
    public final boolean occluding;
    public final String pushReaction; // TODO: Dedicated object?
    public final boolean blockingMotion;
    public final boolean flammable;
    public final boolean liquid;
    public final boolean replaceable;
    public final boolean solid;
    public final boolean solidBlocking;
    public final int mapColorId;
    public final String boundingBox; // TODO: Dedicated object?

    public RawBlockStateData(
            double destroySpeed,
            int lightEmission,
            boolean occluding,
            String pushReaction,
            boolean blockingMotion,
            boolean flammable,
            boolean liquid,
            boolean replaceable,
            boolean solid,
            boolean solidBlocking,
            int mapColorId,
            String boundingBox
    ) {
        this.destroySpeed = destroySpeed;
        this.lightEmission = lightEmission;
        this.occluding = occluding;
        this.pushReaction = pushReaction;
        this.blockingMotion = blockingMotion;
        this.flammable = flammable;
        this.liquid = liquid;
        this.replaceable = replaceable;
        this.solid = solid;
        this.solidBlocking = solidBlocking;
        this.mapColorId = mapColorId;
        this.boundingBox = boundingBox;
    }

    public double getDestroySpeed() {
        return destroySpeed;
    }

    public int getLightEmission() {
        return lightEmission;
    }

    public boolean isOccluding() {
        return occluding;
    }

    public String getPushReaction() {
        return pushReaction;
    }

    public boolean isBlockingMotion() {
        return blockingMotion;
    }

    public boolean isFlammable() {
        return flammable;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public boolean isReplaceable() {
        return replaceable;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isSolidBlocking() {
        return solidBlocking;
    }

    public int getMapColorId() {
        return mapColorId;
    }

    public String getBoundingBox() {
        return boundingBox;
    }
}