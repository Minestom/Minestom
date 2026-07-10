package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the {@link Player} tab list as a {@link Scoreboard}.
 */
public class TabList implements Scoreboard {

    /**
     * <b>WARNING:</b> You shouldn't create scoreboards with the same prefix as those
     */
    private static final String TAB_LIST_PREFIX = "tl-";

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    private final String objectiveName;

    private RenderType type;

    public TabList(String name, RenderType type) {
        this.objectiveName = TAB_LIST_PREFIX + name;

        this.type = type;
    }

    /**
     * Gets the scoreboard objective type
     *
     * @return the scoreboard objective type
     */
    public RenderType getType() {
        return type;
    }

    /**
     * Changes the scoreboard objective type
     *
     * @param type The new type for the objective
     */
    public void setType(RenderType type) {
        this.type = type;
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = this.viewers.add(player);
        if (result) {
            player.sendPacket(this.getCreationObjectivePacket(Component.empty(), this.type));
            player.sendPacket(this.getDisplayScoreboardPacket((byte) 0));
        }
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);
        if (result) {
            player.sendPacket(this.getDestructionObjectivePacket());
        }
        return result;
    }

    @Override
    public Set<? extends Player> getViewers() {
        return unmodifiableViewers;
    }

    @Override
    public String getObjectiveName() {
        return this.objectiveName;
    }
}
