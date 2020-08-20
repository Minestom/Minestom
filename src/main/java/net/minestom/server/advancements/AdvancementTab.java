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

    private static final Map<Player, Set<AdvancementTab>> PLAYER_TAB_MAP = new HashMap<>();

    private final Set<Player> viewers = new HashSet<>();

    private final AdvancementRoot root;

    // Advancement -> its parent
    private final Map<Advancement, Advancement> advancementMap = new HashMap<>();

    // Packet cache, updated every time the tab changes
    protected ByteBuf createBuffer;
    // the packet used to clear the tab (used to remove it and to update an advancement)
    // will never change (since the root identifier is always the same)
    protected ByteBuf removeBuffer;

    protected AdvancementTab(String rootIdentifier, AdvancementRoot root) {
        this.root = root;

        cacheAdvancement(rootIdentifier, root, null);

        final AdvancementsPacket removePacket = AdvancementUtils.getRemovePacket(new String[]{rootIdentifier});
        this.removeBuffer = PacketUtils.writePacket(removePacket);
    }

    /**
     * Get all the tabs of a viewer
     *
     * @param player the player to get the tabs from
     * @return all the advancement tabs that the player sees
     */
    public static Set<AdvancementTab> getTabs(Player player) {
        return PLAYER_TAB_MAP.getOrDefault(player, null);
    }

    /**
     * Get the root advancement of this tab
     *
     * @return the root advancement
     */
    public AdvancementRoot getRoot() {
        return root;
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
    public synchronized boolean addViewer(Player player) {
        final boolean result = viewers.add(player);
        if (!result) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Send the tab to the player
        playerConnection.sendPacket(createBuffer, true);

        addPlayer(player);

        return true;
    }

    @Override
    public synchronized boolean removeViewer(Player player) {
        if (!isViewer(player)) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Remove the tab
        playerConnection.sendPacket(removeBuffer, true);

        removePlayer(player);

        return viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

    /**
     * Add the tab to the player set
     *
     * @param player the player
     */
    private void addPlayer(Player player) {
        Set<AdvancementTab> tabs = PLAYER_TAB_MAP.computeIfAbsent(player, p -> new HashSet<>());
        tabs.add(this);
    }

    /**
     * Remove the tab from the player set
     *
     * @param player the player
     */
    private void removePlayer(Player player) {
        if (!PLAYER_TAB_MAP.containsKey(player)) {
            return;
        }
        Set<AdvancementTab> tabs = PLAYER_TAB_MAP.get(player);
        tabs.remove(this);
        if (tabs.isEmpty()) {
            PLAYER_TAB_MAP.remove(player);
        }
    }

}
