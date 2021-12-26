package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

class BlockPlaceMechanicVerticalPlant {

    static BlockPlaceMechanicVerticalPlant TWISTING_VINES =
            new BlockPlaceMechanicVerticalPlant(1, Block.TWISTING_VINES, Block.TWISTING_VINES_PLANT);
    static BlockPlaceMechanicVerticalPlant WEEPING_VINES =
            new BlockPlaceMechanicVerticalPlant(-1, Block.WEEPING_VINES, Block.WEEPING_VINES_PLANT);
    static BlockPlaceMechanicVerticalPlant BIG_DRIPLEAF =
            new BlockPlaceMechanicVerticalPlant(1, Block.BIG_DRIPLEAF, Block.BIG_DRIPLEAF_STEM);

    private final int offset;
    private final Block mainBlock;
    private final Block stemBlock;

    private BlockPlaceMechanicVerticalPlant(int offset, Block mainBlock, Block stemBlock) {
        this.offset = offset;
        this.mainBlock = mainBlock;
        this.stemBlock = stemBlock;
    }

    void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    private Block update(Block block, Point position, Instance instance) {
        Block checkBlock = instance.getBlock(position.blockX(), position.blockY()+offset, position.blockZ());

        if (checkBlock.compare(mainBlock) || checkBlock.compare(stemBlock)) {
            return block.compare(stemBlock) ? block : cloneProperties(block, stemBlock);
        } else {
            return block.compare(mainBlock) ? block : cloneProperties(block, mainBlock);
        }
    }

    private Block cloneProperties(Block from, Block to) {
        Map<String, String> properties = new HashMap<>(from.properties());
        properties.keySet().retainAll(to.properties().keySet());
        return to.withProperties(properties);
    }

}
