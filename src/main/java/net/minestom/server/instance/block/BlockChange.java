package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface BlockChange permits BlockChange.Instance, BlockChange.Player, BlockChange.Replacement {

    @NotNull
    Block.Getter instance();

    @NotNull
    Point blockPosition();

    @NotNull
    Block block();

    @NotNull
    BlockChange withBlock(@NotNull Block block);

    record Instance(
            @NotNull Block.Getter instance, @NotNull Point blockPosition,
            @NotNull Block block, @Nullable Vec offset
    ) implements BlockChange {

        public Instance(@NotNull Block.Getter instance, @NotNull Point blockPosition, @NotNull Block block) {
            this(instance, blockPosition, block, null);
        }

        @Override
        public @NotNull BlockChange.Instance withBlock(@NotNull Block block) {
            return new Instance(instance, blockPosition, block, offset);
        }
    }

    record Player(
            @NotNull Block.Getter instance, @NotNull Point blockPosition,
            @NotNull Block block, @NotNull BlockFace blockFace,
            @NotNull Player player, @NotNull PlayerHand hand,
            @NotNull Point cursorPosition
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange.Player withBlock(@NotNull Block block) {
            return new Player(instance, blockPosition,
                    block, blockFace,
                    player, hand, cursorPosition);
        }
    }

    record Replacement(
            @NotNull Block.Getter instance, @NotNull Point blockPosition,
            @NotNull Block block, @NotNull BlockFace blockFace,
            @NotNull Point cursorPosition, boolean isOffset,
            @NotNull Material material
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange.Replacement withBlock(@NotNull Block block) {
            return new Replacement(instance, blockPosition, block, blockFace, cursorPosition, isOffset, material);
        }

        public @NotNull BlockChange.Replacement withOffset(boolean offset) {
            return new Replacement(instance, blockPosition, block, blockFace, cursorPosition, offset, material);
        }
    }
}