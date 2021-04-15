package net.minestom.server.raw_data;

import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import net.minestom.server.utils.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class RawBlockData {
    public NamespaceID id;
    public double explosionResistance;
    public double friction;
    public double speedFactor;
    public double jumpFactor;
    public double defaultBlockState;
    public Material item;
    public final List<RawBlockStateData> blockStates = new ArrayList<>();

    @SuppressWarnings("unused")
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
        public MapColor mapColor;
        public String boundingBox; // TODO: Dedicated object
    }
}
