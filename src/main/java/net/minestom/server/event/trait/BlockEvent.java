package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockEvent extends Event {

    @NotNull Block getBlock();

    @NotNull BlockVec getBlockPosition();

    @NotNull BlockEvent.Source getSource();

    /**
     * Represents the source the {@link net.minestom.server.event.trait.BlockEvent} was from
     */
    sealed interface Source permits Source.Instance, Source.Player {

        net.minestom.server.instance.Instance getInstance();

        Instance asInstanceSource();
        Player asPlayerSource();

        record Player(
                @NotNull net.minestom.server.instance.Instance instance,
                @NotNull net.minestom.server.entity.Player player,
                @Nullable BlockFace blockFace,
                @Nullable Point cursorPosition,
                @Nullable PlayerHand hand
        ) implements Source {
            @Override
            public net.minestom.server.instance.Instance getInstance() {
                return instance;
            }

            @Override
            public Instance asInstanceSource() {
                throw new IllegalStateException("Source.Instance cannot be Source.Player!");
            }

            @Override
            public Player asPlayerSource() {
                return this;
            }
        }

        record Instance(@NotNull net.minestom.server.instance.Instance instance) implements Source {
            @Override
            public net.minestom.server.instance.Instance getInstance() {
                return instance;
            }

            @Override
            public Instance asInstanceSource() {
                return this;
            }

            @Override
            public Player asPlayerSource() {
                throw new IllegalStateException("Source.Instance cannot be Source.Player!");
            }
        }

    }
}