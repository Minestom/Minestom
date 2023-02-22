package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
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
        float yaw = pl.getPosition().yaw() + 180;
        int rotation = (int) (Math.round(yaw / 22.5d) % 16);

        // TODO missing banner meta, waiting for https://github.com/Minestom/Minestom/pull/1274

        return block.withProperty("rotation", String.valueOf(rotation));
    }

}
