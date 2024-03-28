package net.minestom.server.instance.heightmap;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface HeightMap {
    NBTCompound getNBT();
    void refresh();
    void refreshAt(int x, int z);
}
