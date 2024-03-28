package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class BannerPlacementRule extends BlockPlacementRule {

    public BannerPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        return block;
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition,
                            @NotNull Player pl) {
        // Can't place at the bottom of a block
        if (blockFace == BlockFace.BOTTOM) {
            return null;
        }

        if (blockFace == BlockFace.TOP) {
            float yaw = pl.getPosition().yaw() + 180;
            int rotation = (int) (Math.round(yaw / 22.5d) % 16);

            return withBannerData(block).withProperty("rotation", String.valueOf(rotation));
        }

        return withBannerData(toWallBlock(block))
                .withProperty("facing", blockFace.name().toLowerCase());
    }

    private Block toWallBlock(Block block) {
        // Same as skulls, maybe there's a better way
        String name = block.namespace().value();

        // white_banner -> white
        String rawName = name.substring(0, name.lastIndexOf("_"));

        return Block.fromNamespaceId(NamespaceID.from(block.namespace().domain(), rawName + "_wall_banner"));
    }

    private Block withBannerData(Block block/*, BannerMeta meta*/) {
        // TODO missing banner meta, waiting for https://github.com/Minestom/Minestom/pull/1274
        //  Also missing ItemMeta from placeBlock() in https://github.com/Minestom/Minestom/pull/1758
        return block;
    }

}
