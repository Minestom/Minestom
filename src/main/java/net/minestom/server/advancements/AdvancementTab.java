package net.minestom.server.advancements;

import io.netty.buffer.ByteBuf;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.advancement.AdvancementUtils;
import net.minestom.server.utils.validate.Check;

import java.util.*;

/**
 * Represents a tab which can be shared between multiple players
 */
public class AdvancementTab implements Viewable {

    private Set<Player> viewers = new HashSet<>();

    // Advancement -> its parent
    private Map<Advancement, Advancement> advancementMap = new HashMap<>();

    // Packet cache, updated every time the tab changes
    protected ByteBuf createBuffer;
    // the packet used to clear the tab (used to remove it and to update an advancement)
    // will never change (since the root identifier is always the same)
    protected ByteBuf removeBuffer;

    protected AdvancementTab(String rootIdentifier, Advancement root) {
        cacheAdvancement(rootIdentifier, root, null);

        final AdvancementsPacket removePacket = AdvancementUtils.getRemovePacket(new String[]{rootIdentifier});
        this.removeBuffer = PacketUtils.writePacket(removePacket);
    }

    /**
     * Create and add an advancement into this tab
     *
     * @param identifier  the unique identifier
     * @param advancement the advancement to add
     * @param parent      the parent of this advancement, it cannot be null
     */
    public void createAdvancement(String identifier, Advancement advancement, Advancement parent) {
        Check.argCondition(identifier == null, "the advancement identifier cannot be null");
        Check.stateCondition(!advancementMap.containsKey(parent),
                "You tried to set a parent which doesn't exist or isn't registered");
        cacheAdvancement(identifier, advancement, parent);
        if (!getViewers().isEmpty()) {
            sendPacketToViewers(advancement.getUpdatePacket());
        }

    }

    /**
     * Update the packet buffer
     */
    protected void updatePacket() {
        this.createBuffer = PacketUtils.writePacket(createPacket());
    }

    /**
     * Build the packet which build the whole advancement tab
     *
     * @return the packet adding this advancement tab and all its advancements
     */
    protected AdvancementsPacket createPacket() {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;

        List<AdvancementsPacket.AdvancementMapping> mappings = new ArrayList<>();
        List<AdvancementsPacket.ProgressMapping> progressMappings = new ArrayList<>();

        for (Advancement advancement : advancementMap.keySet()) {
            mappings.add(advancement.toMapping());
            progressMappings.add(advancement.toProgressMapping());
        }

        advancementsPacket.identifiersToRemove = new String[]{};
        advancementsPacket.advancementMappings = mappings.toArray(new AdvancementsPacket.AdvancementMapping[0]);
        advancementsPacket.progressMappings = progressMappings.toArray(new AdvancementsPacket.ProgressMapping[0]);

        return advancementsPacket;
    }

    /**
     * Cache an advancement
     *
     * @param identifier  the identifier of the advancement
     * @param advancement the advancement
     * @param parent      the parent of this advancement
     */
    private void cacheAdvancement(String identifier, Advancement advancement, Advancement parent) {
        Check.stateCondition(advancement.getTab() != null,
                "You tried to add an advancement already linked to a tab");
        advancement.setTab(this);
        advancement.setIdentifier(identifier);
        advancement.setParent(parent);
        advancement.updateCriteria();
        this.advancementMap.put(advancement, parent);

        updatePacket();
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = viewers.add(player);
        if (!result) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Send the tab to the player
        playerConnection.sendPacket(createBuffer, true);

        return true;
    }

    @Override
    public boolean removeViewer(Player player) {
        if (!isViewer(player)) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Remove the tab
        playerConnection.sendPacket(removeBuffer, true);

        return viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

}
