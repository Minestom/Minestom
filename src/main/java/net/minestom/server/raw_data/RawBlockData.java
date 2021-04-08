package net.minestom.server.raw_data;

import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RawBlockData {
    public NamespaceID id;
    public double explosionResistance;
    public double friction;
    public double speedFactor;
    public double jumpFactor;
    public double defaultBlockState;
    public Material item;
    public final List<RawBlockStateData> blockStates = new ArrayList<>();

    public RawBlockData(
            NamespaceID id,
            double explosionResistance,
            double friction,
            double speedFactor,
            double jumpFactor,
            double defaultBlockState,
            Material item
    ) {
        this.id = id;
        this.explosionResistance = explosionResistance;
        this.friction = friction;
        this.speedFactor = speedFactor;
        this.jumpFactor = jumpFactor;
        this.defaultBlockState = defaultBlockState;
        this.item = item;
    }

    public static class RawBlockStateData {
        public short id;
        public RawBlockData block;
        public double destroySpeed;
        public int lightEmission;
        public boolean occluding;
        public Map<String, String> properties;
        public String pushReaction; //TODO: Dedicated object
        public boolean blocksMotion;
        public boolean isFlammable;
        public boolean isLiquid;
        public boolean isReplaceable;
        public boolean isSolid;
        public boolean isSolidBlocking;
        public int mapColorId; //TODO: Dedicated object
        public String boundingBox; // TODO: Dedicated object

        public RawBlockStateData(
                short id,
                RawBlockData block,
                double destroySpeed,
                int lightEmission,
                boolean occluding,
                Map<String, String> properties,
                String pushReaction,
                boolean blocksMotion,
                boolean isFlammable,
                boolean isLiquid,
                boolean isReplaceable,
                boolean isSolid,
                boolean isSolidBlocking,
                int mapColorId,
                String boundingBox
        ) {
            this.id = id;
            this.block = block;
            this.destroySpeed = destroySpeed;
            this.lightEmission = lightEmission;
            this.occluding = occluding;
            this.properties = properties;
            this.pushReaction = pushReaction;
            this.blocksMotion = blocksMotion;
            this.isFlammable = isFlammable;
            this.isLiquid = isLiquid;
            this.isReplaceable = isReplaceable;
            this.isSolid = isSolid;
            this.isSolidBlocking = isSolidBlocking;
            this.mapColorId = mapColorId;
            this.boundingBox = boundingBox;
        }
    }
}
