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
    protected AdvancementsPacket createPacket() {
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
        playerConnection.sendPacket(removeBuffer, true);

        return viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

}
