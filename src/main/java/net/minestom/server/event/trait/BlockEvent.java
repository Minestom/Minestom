package net.minestom.server.event.trait;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockEvent extends Event {

    /**
     * @return The block involved in the event.
     */
    @NotNull Block getBlock();

    /**
     * @return The position of the block involved in the event.
     */
    @NotNull BlockVec getBlockPosition();

    /**
     * @return The source that triggered the event (e.g., player or instance).
     */
    @NotNull Source getSource();

    /**
     * Represents the source of a block event, which can either be an instance or a player.
     */
    sealed interface Source permits Source.Instance, Source.Player {

        /**
         * @return The instance (world) where the block event occurred.
         */
        @NotNull net.minestom.server.instance.Instance instance();

        /**
         * Attempts to cast this source to an Instance source.
         *
         * @return The source as an Instance source.
         * @throws IllegalStateException if the source is not an instance.
         */
        default @NotNull Source.Instance asInstance() {
            throw new IllegalStateException("%s cannot be converted to Source.Instance".formatted(this));
        }

        /**
         * Attempts to cast this source to a Player source.
         *
         * @return The source as a Player source.
         * @throws IllegalStateException if the source is not a player.
         */
        default @NotNull Source.Player asPlayer() {
            throw new IllegalStateException("%s cannot be converted to Source.Player".formatted(this));
        }

        /**
         * Represents a block event source originating from an instance (world).
         */
        record Instance(@NotNull net.minestom.server.instance.Instance instance) implements Source {
            @Override
            public @NotNull Source.Instance asInstance() {
                return this;
            }
        }

        /**
         * Represents a block event source originating from a player.
         */
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