package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the {@link Player} tab list as a {@link Scoreboard}.
 * <p>
 * This modified version for Hollow Cube mimicks Sidebars so that when players are added as a viewer, the packets aren't
 * needed to be sent out again to all viewers of the TabList.
 * </p>
 */
public class TabList implements Scoreboard {

    /**
     * <b>WARNING:</b> You shouldn't create scoreboards with the same prefix as those
     */
    private static final String TAB_LIST_PREFIX = "tl-";

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    private final String objectiveName;

    private Component header = Component.empty();
    private Component footer = Component.empty();

    private ScoreboardObjectivePacket.Type type;

    public TabList(String name, ScoreboardObjectivePacket.Type type) {
        this.objectiveName = TAB_LIST_PREFIX + name;

        this.type = type;
    }

    /**
     * Gets the scoreboard objective type
     *
     * @return the scoreboard objective type
     */
    public ScoreboardObjectivePacket.Type getType() {
        return type;
    }

    /**
     * Changes the scoreboard objective type
     *
     * @param type The new type for the objective
     */
    public void setType(ScoreboardObjectivePacket.Type type) {
        this.type = type;
    }

    public void setHeader(@NotNull Component header) {
        this.header = header;
        sendPacketToViewers(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    public void setFooter(@NotNull Component footer) {
        this.footer = footer;
        sendPacketToViewers(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        final boolean result = this.viewers.add(player);
        if (result) {
            player.sendPacket(this.getCreationObjectivePacket(Component.empty(), this.type));
            player.sendPacket(this.getDisplayScoreboardPacket((byte) 0));
            player.sendPacket(new PlayerListHeaderAndFooterPacket(header, footer));
        }
        return result;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        final boolean result = this.viewers.remove(player);
        if (result) {
            player.sendPacket(this.getDestructionObjectivePacket());
        }
        return result;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    @Override
    public String getObjectiveName() {
        return this.objectiveName;
    }
}
