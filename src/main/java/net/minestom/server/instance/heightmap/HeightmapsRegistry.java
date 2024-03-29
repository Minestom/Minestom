package net.minestom.server.instance.heightmap;

import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface HeightmapsRegistry {
    NBTCompound getNBT();
    void refreshAt(int x, int y, int z, Block block);
}
