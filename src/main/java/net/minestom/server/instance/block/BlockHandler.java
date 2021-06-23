package net.minestom.server.instance.block;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Interface used to provide block behavior. Set with {@link Block#withHandler(BlockHandler)}.
 * <p>
 * Implementations are expected to be thread safe.
 */
public interface BlockHandler {

    /**
     * Called when a block has been placed.
     *
     * @param placement the placement details
     */
    default void onPlace(@NotNull Placement placement) {
    }

    /**
     * Called when a block has been destroyed or replaced.
     *
     * @param destroy the destroy details
     */
    default void onDestroy(@NotNull Destroy destroy) {
    }

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param interaction the interaction details
     * @return true if this block blocks normal item use, false otherwise
     */
    default boolean onInteract(@NotNull Interaction interaction) {
        return false;
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param touch the contact details
     */
    default void onTouch(@NotNull Touch touch) {
    }

    default void tick(@NotNull Tick tick) {
    }

    default boolean isTickable() {
        return false;
    }

    default @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return Collections.emptyList();
    }

    default byte getBlockEntityAction() {
        return -1;
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the namespace id of this handler
     */
    @NotNull NamespaceID getNamespaceId();

    /**
     * Represents an object forwarded to {@link #onPlace(Placement)}.
     * <p>
     * Will in the future rely on sealed classes (https://openjdk.java.net/jeps/409)
     * and record pattern for the implementations (https://openjdk.java.net/jeps/405).
     */
    @ApiStatus.NonExtendable
    interface Placement {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull BlockPosition blockPosition();

        static @NotNull Placement from(@NotNull Block block, @NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            return new Placement() {
                @Override
                public @NotNull Block block() {
                    return block;
                }

                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull BlockPosition blockPosition() {
                    return blockPosition;
                }
            };
        }
    }

    final class PlayerPlacement implements Placement {
        private final Block block;
        private final Instance instance;
        private final BlockPosition blockPosition;
        private final Player player;
        private final BlockFace blockFace;
        private final float cursorX, cursorY, cursorZ;

        public PlayerPlacement(Block block, Instance instance, BlockPosition blockPosition, Player player,
                               BlockFace blockFace, float cursorX, float cursorY, float cursorZ) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
            this.player = player;
            this.blockFace = blockFace;
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.cursorZ = cursorZ;
        }

        @Override
        public @NotNull Block block() {
            return block;
        }

        @Override
        public @NotNull Instance instance() {
            return instance;
        }

        @Override
        public @NotNull BlockPosition blockPosition() {
            return blockPosition;
        }

        public @NotNull Player player() {
            return player;
        }

        public @NotNull BlockFace blockFace() {
            return blockFace;
        }

        public float cursorX() {
            return cursorX;
        }

        public float cursorY() {
            return cursorY;
        }

        public float cursorZ() {
            return cursorZ;
        }
    }

    @ApiStatus.NonExtendable
    interface Destroy {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull BlockPosition blockPosition();

        static @NotNull Destroy from(@NotNull Block block, @NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            return new Destroy() {
                @Override
                public @NotNull Block block() {
                    return block;
                }

                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull BlockPosition blockPosition() {
                    return blockPosition;
                }
            };
        }
    }

    final class PlayerDestroy implements Destroy {
        private final Block block;
        private final Instance instance;
        private final BlockPosition blockPosition;
        private final Player player;

        public PlayerDestroy(Block block, Instance instance, BlockPosition blockPosition, Player player) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
            this.player = player;
        }

        @Override
        public @NotNull Block block() {
            return block;
        }

        @Override
        public @NotNull Instance instance() {
            return instance;
        }

        @Override
        public @NotNull BlockPosition blockPosition() {
            return blockPosition;
        }

        public @NotNull Player player() {
            return player;
        }
    }

    @ApiStatus.NonExtendable
    interface Interaction {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull BlockPosition blockPosition();

        @NotNull Player player();

        @NotNull Player.Hand hand();

        static @NotNull Interaction from(@NotNull Block block, @NotNull Instance instance, @NotNull BlockPosition blockPosition,
                                         @NotNull Player player, @NotNull Player.Hand hand) {
            return new Interaction() {
                @Override
                public @NotNull Block block() {
                    return block;
                }

                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull BlockPosition blockPosition() {
                    return blockPosition;
                }

                @Override
                public @NotNull Player player() {
                    return player;
                }

                @Override
                public @NotNull Player.Hand hand() {
                    return hand;
                }
            };
        }
    }

    @ApiStatus.NonExtendable
    interface Touch {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull BlockPosition blockPosition();

        @NotNull Entity touching();

        static @NotNull Touch from(@NotNull Block block, @NotNull Instance instance, @NotNull BlockPosition blockPosition,
                                   @NotNull Entity touching) {
            return new Touch() {
                @Override
                public @NotNull Block block() {
                    return block;
                }

                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull BlockPosition blockPosition() {
                    return blockPosition;
                }

                @Override
                public @NotNull Entity touching() {
                    return touching;
                }
            };
        }
    }

    @ApiStatus.NonExtendable
    interface Tick {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull BlockPosition blockPosition();

        static @NotNull Tick from(@NotNull Block block, @NotNull Instance instance, @NotNull BlockPosition blockPosition) {
            return new Tick() {
                @Override
                public @NotNull Block block() {
                    return block;
                }

                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull BlockPosition blockPosition() {
                    return blockPosition;
                }
            };
        }
    }
}
