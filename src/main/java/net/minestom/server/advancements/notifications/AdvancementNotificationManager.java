package net.minestom.server.advancements.notifications;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;

import java.sql.Date;

/**
 * @author Lukas Mansour
 */
public class AdvancementNotificationManager {
    public AdvancementNotificationManager() {

    }

    public void sendAdvancementNotification(AdvancementNotification advancementNotification, Player player) {
        // Sending
        {
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
                    displayData.title = advancementNotification.getTitle();
                    // Description is required, but never shown/seen so, small Easter egg.
                    displayData.description = ColoredText.of("Articdive was here. #Minestom");
                    displayData.icon = advancementNotification.getIcon();
                    displayData.frameType = advancementNotification.getFrameType();
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

                mapping.key = "minestom:advancement_login";
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

                progressMapping.key = "minestom:advancement_login";
                progressMapping.value = advancementProgress;
            }
            advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{progressMapping};

            // Now let's send the the tasty toast.
            player.getPlayerConnection().sendPacket(advancementsPacket);
        }

        // Now we should send a packet telling the player to remove that advancement
        // Removing
        {
            AdvancementsPacket advancementsPacket = new AdvancementsPacket();
            advancementsPacket.resetAdvancements = false;
            advancementsPacket.identifiersToRemove = new String[]{"minestom:advancement_login"};
            advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{};
            advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{};
            player.getPlayerConnection().sendPacket(advancementsPacket);
        }
    }

}
