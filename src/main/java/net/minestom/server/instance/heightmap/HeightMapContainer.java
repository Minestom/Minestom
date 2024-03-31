package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface HeightMapContainer {
    void loadFromNBT(NBTCompound compound);

    NBTCompound getNBT();

    Heightmap getWorldSurface();
    Heightmap getMotionBlocking();

    void refreshAt(int x, int y, int z, Block block);
}
