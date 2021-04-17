package net.minestom.server.raw_data;

import net.minestom.server.map.MapColor;

public class RawBlockStateData {
    public double destroySpeed;
    public int lightEmission;
    public boolean occluding;
    public String pushReaction; // TODO: Dedicated object?
    public boolean blocksMotion;
    public boolean flammable;
    public boolean liquid;
    public boolean replaceable;
    public boolean solid;
    public boolean solidBlocking;
    public MapColor mapColor;
    public String boundingBox; // TODO: Dedicated object?
}