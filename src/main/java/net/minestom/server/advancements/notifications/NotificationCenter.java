package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.advancement.AdvancementUtils;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

/**
 * Used to send one or multiples {@link Notification}.
 * <p>
 * Works by sending a completed advancement and remove it immediately.
 * <p>
 * You can simply create a {@link Notification} object and call {@link #send(Notification, Player)}.
 */
public class NotificationCenter {

    private static final String IDENTIFIER = "minestom:notification";

    /**
     * Can't create an instance, use the static methods instead.
     */
    private NotificationCenter() {
    }

    /**
     * Send a {@link Notification} to one player.
     *
     * @param notification the {@link Notification} to send
     * @param player       the player to send the notification to
     */
    public static void send(Notification notification, Player player) {
        final PlayerConnection playerConnection = player.getPlayerConnection();

        playerConnection.sendPacket(getCreatePacket(notification));

        playerConnection.sendPacket(AdvancementUtils.getRemovePacket(new String[]{IDENTIFIER}));
    }

    /**
     * Send a {@link Notification} to a collection of players.
     *
     * @param notification the {@link Notification} to send
     * @param players      the collection of players to send the notification to
     */
    public static void send(Notification notification, Collection<Player> players) {
        // Can't use PacketWriterUtils because we need the packets to come in the correct order
        players.forEach(player -> send(notification, player));
    }

    /**
     * Create the {@link AdvancementsPacket} responsible for showing the Toast to players
     *
     * @param notification the notification
     * @return the packet used to show the Toast
     */
    private static AdvancementsPacket getCreatePacket(Notification notification) {
        // For An advancement to be shown, it must have all of its criteria achieved (progress 100%)
        // Create a Criteria that we can set to 100% achieved.
        final var displayData = new AdvancementsPacket.DisplayData(
                notification.getTitle(), Component.text("Articdive was here. #Minestom"),
                notification.getIcon(), notification.getFrameType(),
                0x6, null, 0f, 0f);

        final var criteria = new AdvancementsPacket.Criteria("minestom:some_criteria",
                new AdvancementsPacket.CriterionProgress(new Date(System.currentTimeMillis()).getTime()));

        final var advancement = new AdvancementsPacket.Advancement(null, displayData,
                List.of(criteria.criterionIdentifier()), List.of(criteria.criterionIdentifier()));

        final var mapping = new AdvancementsPacket.AdvancementMapping(IDENTIFIER, advancement);
        final var progressMapping = new AdvancementsPacket.ProgressMapping(IDENTIFIER,
                new AdvancementsPacket.AdvancementProgress(List.of(criteria)));

        return new AdvancementsPacket(false, List.of(mapping), List.of(), List.of(progressMapping));
    }
}
