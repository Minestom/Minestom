package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

public interface BlockEvent extends Event {
    @NotNull Block getBlock();

    @NotNull BlockVec getBlockPosition();

    @NotNull Source getSource();

    sealed interface Source permits Source.Instance, Source.Player {

        @NotNull net.minestom.server.instance.Instance instance();

        default @NotNull Source.Instance asInstance() {
            throw new IllegalStateException("%s cannot be converted to Source.Instance".formatted(this));
        }

        default @NotNull Source.Player asPlayer() {
            throw new IllegalStateException("%s cannot be converted to Source.Player".formatted(this));
        }

        record Instance(@NotNull net.minestom.server.instance.Instance instance) implements Source {
            @Override
            public @NotNull Source.Instance asInstance() {
                return this;
            }
        }
        record Player(@NotNull net.minestom.server.instance.Instance instance,
                      @NotNull net.minestom.server.entity.Player player,
                      @Nullable BlockFace blockFace,
                      @NotNull Point cursorPosition,
                      @NotNull PlayerHand playerHand) implements Source {
            @Override
            public @NotNull Source.Player asPlayer() {
                return this;
            }
        }
    }
}
