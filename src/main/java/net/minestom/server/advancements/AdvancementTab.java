package net.minestom.server.advancements;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a tab which can be shared between multiple players. Created using {@link AdvancementManager#createTab(String, AdvancementRoot)}.
 * <p>
 * Each tab requires a root advancement and all succeeding advancements need to have a parent in the tab.
 * You can create a new advancement using {@link #createAdvancement(String, Advancement, Advancement)}.
 * <p>
 * Be sure to use {@link #addViewer(Player)} and {@link #removeViewer(Player)} to control which players can see the tab.
 * (all viewers will see the same tab, with the same amount of validated advancements etc... so shared).
 */
public class AdvancementTab implements Viewable {

    private static final Map<UUID, Set<AdvancementTab>> PLAYER_TAB_MAP = new HashMap<>();

    private final Set<Player> viewers = new HashSet<>();

    private final AdvancementRoot root;

    // Advancement -> its parent
    private final Map<Advancement, Advancement> advancementMap = new HashMap<>();

    // the packet used to clear the tab (used to remove it and to update an advancement)
    // will never change (since the root identifier is always the same)
    protected final AdvancementsPacket removePacket;

    protected AdvancementTab(@NotNull String rootIdentifier, @NotNull AdvancementRoot root) {
        this.root = root;
        cacheAdvancement(rootIdentifier, root, null);
        this.removePacket = new AdvancementsPacket(false, List.of(), List.of(rootIdentifier), List.of());
    }

    /**
     * Gets all the tabs of a viewer.
     *
     * @param player the player to get the tabs from
     * @return all the advancement tabs that the player sees, can be null
     * if the player doesn't see anything
     */
    @Nullable
    public static Set<AdvancementTab> getTabs(@NotNull Player player) {
        return PLAYER_TAB_MAP.getOrDefault(player.getUuid(), null);
    }

    /**
     * Gets the root advancement of this tab.
     *
     * @return the root advancement
     */
    @NotNull
    public AdvancementRoot getRoot() {
        return root;
    }

    /**
     * Creates and add an advancement into this tab.
     *
     * @param identifier  the unique identifier
     * @param advancement the advancement to add
     * @param parent      the parent of this advancement, it cannot be null
     */
    public void createAdvancement(@NotNull String identifier, @NotNull Advancement advancement, @NotNull Advancement parent) {
        Check.stateCondition(!advancementMap.containsKey(parent),
                "You tried to set a parent which doesn't exist or isn't registered");
        cacheAdvancement(identifier, advancement, parent);
        if (!getViewers().isEmpty()) {
            sendPacketToViewers(advancement.getUpdatePacket());
        }

    }

    /**
     * Builds the packet which build the whole advancement tab.
     *
     * @return the packet adding this advancement tab and all its advancements
     */
    protected @NotNull AdvancementsPacket createPacket() {
        List<AdvancementsPacket.AdvancementMapping> mappings = new ArrayList<>();
        List<AdvancementsPacket.ProgressMapping> progressMappings = new ArrayList<>();
        for (Advancement advancement : advancementMap.keySet()) {
            mappings.add(advancement.toMapping());
            progressMappings.add(advancement.toProgressMapping());
        }
        return new AdvancementsPacket(false, mappings, List.of(), progressMappings);
    }

    /**
     * Caches an advancement.
     *
     * @param identifier  the identifier of the advancement
     * @param advancement the advancement
     * @param parent      the parent of this advancement, only null for the root advancement
     */
    private void cacheAdvancement(@NotNull String identifier, @NotNull Advancement advancement, @Nullable Advancement parent) {
        Check.stateCondition(advancement.getTab() != null,
                "You tried to add an advancement already linked to a tab");
        advancement.setTab(this);
        advancement.setIdentifier(identifier);
        advancement.setParent(parent);
        advancement.updateCriteria();
        this.advancementMap.put(advancement, parent);
    }

    @Override
    public synchronized boolean addViewer(@NotNull Player player) {
        final boolean result = viewers.add(player);
        if (!result) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Send the tab to the player
        playerConnection.sendPacket(createPacket());

        addPlayer(player);

        return true;
    }

    @Override
    public synchronized boolean removeViewer(@NotNull Player player) {
        if (!isViewer(player)) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Remove the tab
        if (!player.isRemoved()) {
            playerConnection.sendPacket(removePacket);
        }

        removePlayer(player);

        return viewers.remove(player);
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

    /**
     * Adds the tab to the player set.
     *
     * @param player the player
     */
    private void addPlayer(@NotNull Player player) {
        Set<AdvancementTab> tabs = PLAYER_TAB_MAP.computeIfAbsent(player.getUuid(), p -> new HashSet<>());
        tabs.add(this);
    }

    /**
     * Removes the tab from the player set.
     *
     * @param player the player
     */
    private void removePlayer(@NotNull Player player) {
        final UUID uuid = player.getUuid();
        if (!PLAYER_TAB_MAP.containsKey(uuid)) {
            return;
        }
        Set<AdvancementTab> tabs = PLAYER_TAB_MAP.get(uuid);
        tabs.remove(this);
        if (tabs.isEmpty()) {
            PLAYER_TAB_MAP.remove(uuid);
        }
    }

}
