package net.minestom.server.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Is used to send temporary advancements to the client, which are called notifications.
 * <br>
 * Here is an example of its use:
 * <pre><code>
 * Notification notification = Notification.builder()
 *  .frameType(FrameType.TASK)
 *  .title(Component.text("Welcome!"))
 *  .icon(Material.IRON_SWORD).build();
 * notification.send(player);
 * </code></pre>
 *
 * The constant {@link #IDENTIFIER} is used for the advancement packet
 * The constant {@link #REMOVE_PACKET} is used to remove previous notifications
 * @since 1.4.1
 */
public sealed interface Notification permits NotificationImpl {

    String IDENTIFIER = "minestom:notification";
    AdvancementsPacket REMOVE_PACKET = new AdvancementsPacket(false, List.of(), List.of(IDENTIFIER), List.of());

    /**
     * Creates a new builder instance
     * @return an instance of the builder
     */
    @Contract(pure = true)
    static @NotNull Builder builder() {
        return new NotificationBuilder();
    }

    /**
     * Send the notification to the client
     * @param player to get be sent
     */
    void send(@NotNull Player player);

    /**
     * Send the notification to a collection of clients
     * @param players to get be sent
     */
    void send(@NotNull Collection<@NotNull Player> players);

    /**
     * Gets the title of the notification as a {@link Component}
     * @return the title {@link Component}
     */
    @NotNull Component title();

    /**
     * Get the {@link FrameType} of the notification
     * @return the type
     */
    @NotNull FrameType type();

    /**
     * Get the displayed icon of the notification as {@link ItemStack}
     * @return the {@link ItemStack}
     */
    @NotNull ItemStack icon();

    /**
     * @since 1.4.1
     */
    sealed interface Builder permits NotificationBuilder {

        /**
         * Set the title for a notification as component.
         *
         * If you're using a resource pack you can use {@link Component#translatable(String)}
         *
         * @param component to get send to the client
         * @return the builder
         */
        Builder title(@NotNull Component component);

        /**
         * Set the frame typ of the notification
         * @param frameType to showed for the client
         * @return the builder
         */
        Builder frameType(@NotNull FrameType frameType);

        /**
         * Set the {@link Material} for the icon
         * @param material to be shown to the client
         * @return the builder
         */
        Builder icon(@NotNull Material material);

        /**
         * Set the {@link ItemStack} for the icon
         * @param itemStack to be shown to the client
         * @return the builder
         */
        Builder icon(@NotNull ItemStack itemStack);

        /**
         * Returns an instance of the creation notification
         * @return the instance
         */
        Notification build();
    }
}
