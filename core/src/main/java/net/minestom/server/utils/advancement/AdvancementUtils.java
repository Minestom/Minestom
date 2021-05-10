package net.minestom.server.utils.advancement;

import net.minestom.server.network.packet.server.play.AdvancementsPacket;

public final class AdvancementUtils {

    private AdvancementUtils() {

    }

    /**
     * Gets an {@link AdvancementsPacket} which remove the specified identifiers.
     *
     * @param identifiers the identifiers to remove
     * @return the packet to remove all the identifiers
     */
    public static AdvancementsPacket getRemovePacket(String[] identifiers) {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;
        advancementsPacket.identifiersToRemove = identifiers;
        advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{};
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{};

        return advancementsPacket;
    }
}
