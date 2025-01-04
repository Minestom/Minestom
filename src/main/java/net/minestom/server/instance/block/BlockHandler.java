package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     * @return true to let the block interaction happens, false to cancel
     */
    default boolean onInteract(@NotNull Interaction interaction) {
        return true;
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

    /**
     * Determines if we should call {@link #tick(Tick)}
     *
     * Warning: Changing this without setting the block can lead to undefined ticking behavior.
     *
     * @return true to allow {@link #tick(Tick)} to be called
     */
    default boolean tickable() {
        return false;
    }

    /**
     * Specifies which block entity tags should be sent to the player.
     *
     * @return The list of tags from this block's block entity that should be sent to the player
     * @see <a href="https://minecraft.wiki/w/Block_entity">Block entity on the Minecraft wiki</a>
     */
    default @NotNull Collection<Tag<?>> blockEntityTags() {
        return List.of();
    }

    default byte blockEntityAction() {
        return -1;
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the namespace id of this handler
     */
    @NotNull NamespaceID namespaceId();

    /**
     * Represents an object forwarded to {@link #onPlace(Placement)}.
     */
    sealed interface Placement permits PlacementImpl, PlayerPlacement {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();
    }

    sealed interface PlayerPlacement extends Placement permits PlayerPlacementImpl {
        @NotNull Player player();

        @NotNull PlayerHand hand();

        @NotNull BlockFace blockFace();

        float cursorX();

        float cursorY();

        float cursorZ();
    }

    /**
     * Represents an object forwarded to {@link #onDestroy(Destroy)}.
     */
    sealed interface Destroy permits DestroyImpl, PlayerDestroy {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();
    }

    sealed interface PlayerDestroy extends Destroy permits PlayerDestroyImpl {
        @NotNull Player player();
    }

    @ApiStatus.Internal
    record PlacementImpl(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition) implements Placement {
    }

    @ApiStatus.Internal
    record PlayerPlacementImpl(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition, @NotNull Player player, @NotNull PlayerHand hand,
                               @NotNull BlockFace blockFace,
                               float cursorX, float cursorY, float cursorZ) implements PlayerPlacement {
    }

    @ApiStatus.Internal
    record DestroyImpl(@NotNull Block block, @NotNull Instance instance,
                       @NotNull Point blockPosition) implements Destroy {
    }

    @ApiStatus.Internal
    record PlayerDestroyImpl(@NotNull Block block, @NotNull Instance instance,
                             @NotNull Point blockPosition, @NotNull Player player) implements PlayerDestroy {
    }

    @ApiStatus.Internal
    record Interaction(@NotNull Block block, @NotNull Instance instance, @NotNull BlockFace blockFace,
                       @NotNull Point blockPosition, @NotNull Point cursorPosition, @NotNull Player player,
                       @NotNull PlayerHand hand) {
    }

    @ApiStatus.Internal
    record Touch(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition,
                 @NotNull Entity touching) {
    }

    @ApiStatus.Internal
    record Tick(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition) {
    }

    /**
     * Handler used for loaded blocks with unknown namespace
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    record Dummy(NamespaceID namespaceId) implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        public static @NotNull BlockHandler get(@NotNull String namespace) {
            return DUMMY_CACHE.computeIfAbsent(namespace, Dummy::new);
        }

        private Dummy(String name) {
            this(NamespaceID.from(name));
        }

    }
}
