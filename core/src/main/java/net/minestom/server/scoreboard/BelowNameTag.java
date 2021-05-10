package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a scoreboard which rendered a tag below the name.
 */
public class BelowNameTag implements Scoreboard {

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

    @Override
    public @NotNull String getObjectiveName() {
        return this.objectiveName;
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        boolean result = this.viewers.add(player);
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.scoreboardObjectivePacket);
            connection.sendPacket(this.getDisplayScoreboardPacket((byte) 2));

            player.setBelowNameTag(this);
        }

        return result;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        boolean result = this.viewers.remove(player);
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.getDestructionObjectivePacket());
            player.setBelowNameTag(null);
        }

        return result;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }
}
