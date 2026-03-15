package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a scoreboard which rendered a tag below the name.
 * @deprecated Use a Scoreboard instead
 */
@Deprecated
public class BelowNameTag {

    /**
     * <b>WARNING:</b> You shouldn't create scoreboards with the same prefix as those
     */
    public static final String BELOW_NAME_TAG_PREFIX = "bnt-";

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    private final String objectiveName;

    private final ScoreboardObjectivePacket scoreboardObjectivePacket;

    /**
     * Creates a new below name scoreboard.
     *
     * @param name  The objective name of the scoreboard
     * @param value The value of the scoreboard
     * @deprecated Use {@link #BelowNameTag(String, Component)}
     */
    @Deprecated
    public BelowNameTag(String name, String value) {
        this(name, Component.text(value));
    }

    /**
     * Creates a new below name scoreboard.
     *
     * @param name  The objective name of the scoreboard
     * @param value The value of the scoreboard
     */
    public BelowNameTag(String name, Component value) {
        this.objectiveName = BELOW_NAME_TAG_PREFIX + name;
        this.scoreboardObjectivePacket = this.getCreationObjectivePacket(value, ScoreboardObjectivePacket.Type.INTEGER);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public boolean addViewer(Player player) {
        final boolean result = this.viewers.add(player);
        if (result) {
            player.sendPacket(this.scoreboardObjectivePacket);
            player.sendPacket(this.getDisplayScoreboardPacket((byte) 2));
            player.setBelowNameTag(this);
        }
        return result;
    }

    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);
        if (result) {
            player.sendPacket(this.getDestructionObjectivePacket());
            player.setBelowNameTag(null);
        }
        return result;
    }

    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    /**
     * Creates a creation objective packet.
     *
     * @param value The value for the objective
     * @param type  The type for the objective
     * @return the creation objective packet
     */
    public ScoreboardObjectivePacket getCreationObjectivePacket(Component value, ScoreboardObjectivePacket.Type type) {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 0, value, type, null);
    }

    /**
     * Creates the destruction objective packet.
     *
     * @return the destruction objective packet
     */
    public ScoreboardObjectivePacket getDestructionObjectivePacket() {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 1, null, null, null);
    }

    /**
     * Creates the {@link DisplayScoreboardPacket}.
     *
     * @param position The position of the scoreboard
     * @return the created display scoreboard packet
     */
    public DisplayScoreboardPacket getDisplayScoreboardPacket(byte position) {
        return new DisplayScoreboardPacket(position, getObjectiveName());
    }
}
