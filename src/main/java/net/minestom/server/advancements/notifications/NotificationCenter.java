package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.advancement.AdvancementUtils;

import java.sql.Date;
import java.util.Collection;

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
        // For An advancement to be shown, it must have all of it's criteria achieved (progress 100%)
        // Create a Criteria that we can set to 100% achieved.
        // Criteria

        AdvancementsPacket.Criteria criteria = new AdvancementsPacket.Criteria();
        {
            AdvancementsPacket.CriterionProgress progress = new AdvancementsPacket.CriterionProgress();
            progress.achieved = true;
            progress.dateOfAchieving = new Date(System.currentTimeMillis()).getTime();
            criteria.criterionProgress = progress;
            criteria.criterionIdentifier = "minestom:some_criteria";
        }

        // Now create an AdvancementsPacket that we can send:
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;

        AdvancementsPacket.AdvancementMapping mapping = new AdvancementsPacket.AdvancementMapping();
        {
            // Get the advancement
            AdvancementsPacket.Advancement advancement = new AdvancementsPacket.Advancement();
            // Setup display data for the advancement
            AdvancementsPacket.DisplayData displayData = new AdvancementsPacket.DisplayData();
            {
                displayData.title = notification.getTitle();
                // Description is required, but never shown/seen so, small Easter egg.
                displayData.description = Component.text("Articdive was here. #Minestom");
                displayData.icon = notification.getIcon();
                displayData.frameType = notification.getFrameType();
                displayData.flags = 0x6;
                // No background texture required as we are using 0x6
                displayData.x = 0.0F;
                displayData.y = 0.0F;
            }
            advancement.displayData = displayData;
            // Add the criteria to the advancement
            advancement.criterions = new String[]{criteria.criterionIdentifier};
            // Add the requirement of the criteria to the advancement
            AdvancementsPacket.Requirement requirement = new AdvancementsPacket.Requirement();
            {
                requirement.requirements = new String[]{criteria.criterionIdentifier};
            }
            advancement.requirements = new AdvancementsPacket.Requirement[]{requirement};

            mapping.key = IDENTIFIER;
            mapping.value = advancement;
        }
        // Add the mapping to the main packet
        advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{mapping};


        // We have no identifiers to remove.
        advancementsPacket.identifiersToRemove = new String[]{};

        // Now we need to set the player's progress for the criteria.
        AdvancementsPacket.ProgressMapping progressMapping = new AdvancementsPacket.ProgressMapping();
        {
            AdvancementsPacket.AdvancementProgress advancementProgress = new AdvancementsPacket.AdvancementProgress();
            advancementProgress.criteria = new AdvancementsPacket.Criteria[]{criteria};

            progressMapping.key = IDENTIFIER;
            progressMapping.value = advancementProgress;
        }
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{progressMapping};

        return advancementsPacket;
    }

}
