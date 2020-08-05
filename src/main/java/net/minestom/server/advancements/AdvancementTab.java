package net.minestom.server.advancements;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;

import java.util.*;

public class AdvancementTab implements Viewable {

    private Set<Player> viewers = new HashSet<>();

    private Advancement root;

    // Advancement -> its parent
    private Map<Advancement, Advancement> advancementMap = new HashMap<>();

    protected AdvancementTab(String rootIdentifier, Advancement root) {
        this.root = root;
        cacheAdvancement(rootIdentifier, root, null);
    }

    public void createAdvancement(String identifier, Advancement advancement, Advancement parent) {
        Check.stateCondition(!advancementMap.containsKey(parent),
                "You tried to set a parent which doesn't exist or isn't registered");
        cacheAdvancement(identifier, advancement, parent);
    }

    /**
     * Build the packet which build the whole advancement tab
     *
     * @return the packet adding this advancement tab and all its advancements
     */
    public AdvancementsPacket createPacket() {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;

        List<AdvancementsPacket.AdvancementMapping> mappings = new ArrayList<>();

        for (Advancement advancement : advancementMap.keySet()) {
            AdvancementsPacket.AdvancementMapping mapping = new AdvancementsPacket.AdvancementMapping();
            {
                AdvancementsPacket.Advancement adv = new AdvancementsPacket.Advancement();
                mapping.key = advancement.getIdentifier();
                mapping.value = adv;

                final Advancement parent = advancement.getParent();
                if (parent != null) {
                    final String parentIdentifier = parent.getIdentifier();
                    adv.parentIdentifier = parentIdentifier;
                }

                adv.displayData = advancement.toDisplayData();
                adv.criterions = new String[]{};
                adv.requirements = new AdvancementsPacket.Requirement[]{};
            }
            mappings.add(mapping);
        }

        advancementsPacket.identifiersToRemove = new String[]{};
        advancementsPacket.advancementMappings = mappings.toArray(new AdvancementsPacket.AdvancementMapping[0]);
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{};

        return advancementsPacket;
    }

    /**
     * Create a packet which remove the root advancement
     * <p>
     * This does in fact remove the whole advancement tab
     *
     * @return the packet which remove the root advancement
     */
    public AdvancementsPacket removePacket() {
        AdvancementsPacket advancementsPacket = new AdvancementsPacket();
        advancementsPacket.resetAdvancements = false;
        advancementsPacket.identifiersToRemove = new String[]{root.getIdentifier()};
        advancementsPacket.advancementMappings = new AdvancementsPacket.AdvancementMapping[]{};
        advancementsPacket.progressMappings = new AdvancementsPacket.ProgressMapping[]{};

        return advancementsPacket;
    }

    private void cacheAdvancement(String identifier, Advancement advancement, Advancement parent) {
        Check.stateCondition(advancement.getTab() != null,
                "You tried to add an advancement already linked to a tab");
        advancement.setTab(this);
        advancement.setIdentifier(identifier);
        advancement.setParent(parent);
        this.advancementMap.put(advancement, parent);
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = viewers.add(player);
        if (!result) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Send the tab to the player
        playerConnection.sendPacket(createPacket());

        return true;
    }

    @Override
    public boolean removeViewer(Player player) {
        if (!isViewer(player)) {
            return false;
        }

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Remove the tab
        playerConnection.sendPacket(removePacket());

        return viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return viewers;
    }
}
