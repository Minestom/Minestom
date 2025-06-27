package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import org.jetbrains.annotations.NotNull;

public sealed interface BlockChange {

    @NotNull
    Block.Getter instance();

    @NotNull
    Point blockPosition();

    @NotNull
    Block block();

    @NotNull
    BlockChange withBlock(@NotNull Block newBlock);

    record Instance(
        @NotNull Block.Getter instance, @NotNull Point blockPosition,
        @NotNull Block block
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange.Instance withBlock(@NotNull Block newBlock) {
            return new Instance(instance, blockPosition, newBlock);
        }
    }

    record Player(
        @NotNull Block.Getter instance, @NotNull Point blockPosition,
        @NotNull Block block, @NotNull BlockFace blockFace,
        @NotNull net.minestom.server.entity.Player player, @NotNull PlayerHand hand,
        @NotNull Point cursorPosition
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange.Player withBlock(@NotNull Block newBlock) {
            return new Player(instance, blockPosition,
                                newBlock, blockFace,
                                player, hand, cursorPosition);
        }
    }

    record Replacement(
        @NotNull Block.Getter instance, @NotNull Point blockPosition,
        @NotNull Block block, @NotNull BlockFace blockFace,
        @NotNull Point cursorPosition, boolean isOffset,
        @NotNull net.minestom.server.item.Material material
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange.Replacement withBlock(@NotNull Block newBlock) {
            return new Replacement(instance, blockPosition, newBlock, blockFace, cursorPosition, isOffset, material);
        }

        public @NotNull BlockChange.Replacement withOffset(boolean newOffset) {
            return new Replacement(instance, blockPosition, block, blockFace, cursorPosition, newOffset, material);
        }
    }
}