package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;

public interface BlockStorage extends Block.Getter, Block.Setter {

    @ApiStatus.Experimental
    void track(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax);
}
