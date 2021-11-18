package net.minestom.server.utils.advancement;

import net.minestom.server.network.packet.server.play.AdvancementsPacket;

import java.util.List;

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
        return new AdvancementsPacket(false, List.of(), List.of(identifiers), List.of());
    }
}
