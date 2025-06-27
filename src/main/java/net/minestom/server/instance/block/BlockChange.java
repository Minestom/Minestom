package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface BlockChange {

    @NotNull
    Block.Getter instance();

    @NotNull
    Point blockPosition();

    @NotNull
    Block block();

    @Nullable
    BlockFace blockFace();

    @NotNull
    BlockChange withBlock(@NotNull Block newBlock);

    record Instance(
        @NotNull Block.Getter instance, @NotNull Point blockPosition,
        @NotNull Block block, @Nullable BlockFace blockFace
    ) implements BlockChange {

        @Override
        public @NotNull BlockChange withBlock(@NotNull Block newBlock) {
            return new Instance(instance, blockPosition, newBlock, blockFace);
        }
    }

    record Player(
        @NotNull Block.Getter instance, @NotNull Point blockPosition,
        @NotNull Block block, @NotNull BlockFace blockFace,
        @NotNull net.minestom.server.entity.Player player
    ) implements BlockChange {

        public @NotNull net.minestom.server.entity.Player player() {
            return player;
        }

        @Override
        public @NotNull BlockChange.Player withBlock(@NotNull Block newBlock) {
            return new Player(instance, blockPosition, newBlock, blockFace, player);
        }
    }
}