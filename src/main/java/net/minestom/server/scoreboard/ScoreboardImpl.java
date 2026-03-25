package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A scoreboard that stores score and line format per entry.
 */
public class ScoreboardImpl implements Scoreboard {

    protected final String objectiveName;
    protected @Nullable Component displayName;
    protected Position position;
    protected ScoreboardObjectivePacket.Type displayType = ScoreboardObjectivePacket.Type.INTEGER;
    protected @Nullable NumberFormat defaultNumberFormat;

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public record Entry(int score, @Nullable Component displayName, @Nullable NumberFormat numberFormat) {
        public static final Entry DEFAULT = new Entry(0, null, null);

        public UpdateScorePacket getUpdateScorePacket(String entity, String objective) {
            return new UpdateScorePacket(entity, objective, score, displayName, numberFormat);
        }

        public Entry withScore(int score) {
            return new Entry(score, displayName, numberFormat);
        }

        public Entry withDisplayName(@Nullable Component displayName) {
            return new Entry(score, displayName, numberFormat);
        }

        public Entry withNumberFormat(@Nullable NumberFormat numberFormat) {
            return new Entry(score, displayName, numberFormat);
        }
    }

    ScoreboardImpl(String objectiveName, Position position) {
        this.objectiveName = objectiveName;
        this.position = position;
    }

    /**
     * Adds a viewer to the scoreboard. Use {@link Player#showScoreboard(Scoreboard)} instead.
     * @param player the viewer to add
     * @return true if the player has been added, false otherwise
     */
    @Override
    @ApiStatus.Internal
    public boolean addViewer(Player player) {
        boolean added = viewers.add(player);
        if (!added) return false;
        player.sendPackets(getCreationObjectivePacket(), getDisplayScoreboardPacket(position.asByte()));
        entries.forEach((entity, entry) ->
                player.sendPacket(entry.getUpdateScorePacket(entity, objectiveName))
        );
        return true;
    }

    /**
     * Removes a viewer from the scoreboard. Use {@link Player#hideScoreboard(Scoreboard)} instead.
     * @param player the viewer to remove
     * @return true if the player has been removed, false otherwise
     */
    @Override
    @ApiStatus.Internal
    public boolean removeViewer(Player player) {
        boolean removed = viewers.remove(player);
        if (!removed) return false;
        player.sendPacket(getDestructionObjectivePacket());
        return true;
    }

    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    public ScoreboardObjectivePacket getCreationObjectivePacket() {
        return new ScoreboardObjectivePacket(objectiveName, (byte) 0, displayName, displayType, defaultNumberFormat);
    }

    public ScoreboardObjectivePacket getUpdateObjectivePacket() {
        return new ScoreboardObjectivePacket(objectiveName, (byte) 2, displayName, displayType, defaultNumberFormat);
    }

    public ScoreboardObjectivePacket getDestructionObjectivePacket() {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 1, null, null, null);
    }

    public DisplayScoreboardPacket getDisplayScoreboardPacket(byte position) {
        return new DisplayScoreboardPacket(position, objectiveName);
    }

    @Override
    public String getObjectiveName() {
        return objectiveName;
    }

    @Override
    public @Nullable Component getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
        sendObjectiveUpdate();
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;
        sendPacketToViewers(new DisplayScoreboardPacket(position.asByte(), objectiveName));
    }

    @Override
    public ScoreboardObjectivePacket.Type getDisplayType() {
        return displayType;
    }

    @Override
    public void setDisplayType(ScoreboardObjectivePacket.Type displayType) {
        this.displayType = displayType;
        sendObjectiveUpdate();
    }

    @Override
    public @Nullable NumberFormat getDefaultNumberFormat() {
        return defaultNumberFormat;
    }

    @Override
    public void setDefaultNumberFormat(@Nullable NumberFormat defaultNumberFormat) {
        this.defaultNumberFormat = defaultNumberFormat;
        sendObjectiveUpdate();
    }

    @Override
    public void updateScore(String entity, int score) {
        Entry entry = entries.compute(entity, (_, current) ->
            (current != null ? current : Entry.DEFAULT).withScore(score)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateDisplayName(String entity, @Nullable Component displayName) {
        Entry entry = entries.compute(entity, (_, current) ->
                (current != null ? current : Entry.DEFAULT).withDisplayName(displayName)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateNumberFormat(String entity, @Nullable NumberFormat numberFormat) {
        Entry entry = entries.compute(entity, (_, current) ->
                (current != null ? current : Entry.DEFAULT).withNumberFormat(numberFormat)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateEntry(String entity, int score, @Nullable Component displayName, @Nullable NumberFormat numberFormat) {
        Entry entry = new Entry(score, displayName, numberFormat);
        entries.put(entity, entry);
        sendUpdate(entity, entry);
    }

    public void removeScore(String entity) {
        if (entries.remove(entity) == null) return;
        sendPacketToViewers(new ResetScorePacket(entity, objectiveName));
    }

    public void sendUpdate(String entity, Entry entry) {
        sendPacketToViewers(entry.getUpdateScorePacket(entity, objectiveName));
    }

    public void sendObjectiveUpdate() {
        sendPacketToViewers(getUpdateObjectivePacket());
    }

    @Override
    public Collection<Player> getPlayers() {
        return Scoreboard.super.getPlayers();
    }
}
