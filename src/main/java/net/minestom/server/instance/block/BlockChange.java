package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

public sealed interface BlockChange permits BlockChange.Instance, BlockChange.Player, BlockChange.Replacement {
    
    Block.Getter instance();

    Point blockPosition();

    Block block();

    BlockChange withBlock(Block block);

    record Instance(
            Block.Getter instance, Point blockPosition,
            Block block, @Nullable Vec offset
    ) implements BlockChange {

        public Instance(Block.Getter instance, Point blockPosition, Block block) {
            this(instance, blockPosition, block, null);
        }

        @Override
        public BlockChange.Instance withBlock(Block block) {
            return new Instance(instance, blockPosition, block, offset);
        }
    }

    record Player(
        Block.Getter instance, Point blockPosition,
        Block block, BlockFace blockFace,
        net.minestom.server.entity.Player player, PlayerHand hand,
        Point cursorPosition
    ) implements BlockChange {

        @Override
        public BlockChange.Player withBlock(Block block) {
            return new Player(instance, blockPosition,
                    block, blockFace,
                    player, hand, cursorPosition);
        }
    }

    record Replacement(
            Block.Getter instance, Point blockPosition,
            Block block, BlockFace blockFace,
            Point cursorPosition, boolean isOffset,
            Material material
    ) implements BlockChange {

        @Override
        public BlockChange.Replacement withBlock(Block block) {
            return new Replacement(instance, blockPosition, block, blockFace, cursorPosition, isOffset, material);
        }

        public BlockChange.Replacement withOffset(boolean offset) {
            return new Replacement(instance, blockPosition, block, blockFace, cursorPosition, offset, material);
        }
    }
}