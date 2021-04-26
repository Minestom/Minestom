package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.acquirable.AcquirableCollection;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the {@link Player} tab list as a {@link Scoreboard}.
 */
public class TabList implements Scoreboard {

    /**
     * <b>WARNING:</b> You shouldn't create scoreboards with the same prefix as those
     */
    private static final String TAB_LIST_PREFIX = "tl-";

    private final AcquirableCollection<Player> viewers = new AcquirableCollection<>(new CopyOnWriteArraySet<>());
    private final String objectiveName;

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

    @Override
    public boolean addViewer(@NotNull Player player) {
        boolean result = this.viewers.add(player.getAcquirable());
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.getCreationObjectivePacket(Component.empty(), this.type));
            connection.sendPacket(this.getDisplayScoreboardPacket((byte) 0));
        }

        return result;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        boolean result = this.viewers.remove(player.getAcquirable());
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.getDestructionObjectivePacket());
        }

        return result;
    }

    @Override
    public @NotNull AcquirableCollection<Player> getViewers() {
        return viewers;
    }

    @Override
    public String getObjectiveName() {
        return this.objectiveName;
    }
}
