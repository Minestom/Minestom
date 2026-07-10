package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/// A scoreboard that stores score and line format per entry.
final class ScoreboardImpl implements Scoreboard {

    private final String objectiveName;
    private Component displayName;
    private RenderType renderType = RenderType.INTEGER;
    private @Nullable NumberFormat defaultNumberFormat;

    private final Map<Player, Set<Position>> viewers = new ConcurrentHashMap<>();
    private final Map<Player, Set<Position>> unmodifiableViewers = Collections.unmodifiableMap(viewers);
    private final Map<String, ScoreEntry> entries = new ConcurrentHashMap<>();
    private final Map<String, ScoreEntry> unmodifiableEntries = Collections.unmodifiableMap(entries);

    ScoreboardImpl(String objectiveName) {
        this(objectiveName, Component.text(objectiveName));
    }

    ScoreboardImpl(String objectiveName, Component displayName) {
        this.objectiveName = objectiveName;
        this.displayName = displayName;
    }

    @Override
    public boolean addViewer(Player player, Position position) {
        Set<Position> added = viewers.putIfAbsent(player, EnumSet.of(position));
        if (added == null) {
            player.sendPacket(getCreationObjectivePacket());
            entries.forEach((entity, entry) ->
                    player.sendPacket(entry.getUpdateScorePacket(entity, objectiveName))
            );
        } else {
            if (!added.add(position)) return false;
        }
        player.sendPacket(new DisplayScoreboardPacket(position.asByte(), objectiveName));
        return true;
    }

    @Override
    public boolean removeViewer(Player player, Position position) {
        Set<Position> removed = viewers.get(player);
        if (removed == null) return false;
        if (!removed.remove(position)) return false;
        player.sendPacket(new DisplayScoreboardPacket(position.asByte(), "")); // Matches vanilla
        if (removed.isEmpty()) {
            player.sendPacket(getDestructionObjectivePacket());
            viewers.remove(player);
        }
        return true;
    }

    @Override
    public boolean removeViewer(Player player) {
        Set<Position> removed = viewers.remove(player);
        if (removed == null) return false;
        player.sendPacket(getDestructionObjectivePacket());
        return true;
    }

    @Override
    public boolean isViewer(Player player) {
        return viewers.containsKey(player);
    }

    @Override
    public boolean isViewer(Player player, Position position) {
        Set<Position> positions = viewers.get(player);
        return positions != null && positions.contains(position);
    }

    @Override
    public Map<Player, Set<Position>> getViewers() {
        return unmodifiableViewers;
    }

    public ScoreboardObjectivePacket getCreationObjectivePacket() {
        return new ScoreboardObjectivePacket(objectiveName, (byte) 0, displayName, renderType, defaultNumberFormat);
    }

    public ScoreboardObjectivePacket getUpdateObjectivePacket() {
        return new ScoreboardObjectivePacket(objectiveName, (byte) 2, displayName, renderType, defaultNumberFormat);
    }

    public ScoreboardObjectivePacket getDestructionObjectivePacket() {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 1, null, null, null);
    }

    @Override
    public String getObjectiveName() {
        return objectiveName;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
        sendObjectiveUpdate();
    }

    @Override
    public RenderType getRenderType() {
        return renderType;
    }

    @Override
    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
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
    public @Nullable ScoreEntry getEntry(String entity) {
        return entries.get(entity);
    }

    @Override
    public Map<String, ScoreEntry> getEntries() {
        return unmodifiableEntries;
    }

    @Override
    public void updateScore(String entity, int score) {
        ScoreEntry entry = entries.compute(entity, (_, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withScore(score)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateDisplayName(String entity, @Nullable Component displayName) {
        ScoreEntry entry = entries.compute(entity, (_, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withDisplayName(displayName)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateNumberFormat(String entity, @Nullable NumberFormat numberFormat) {
        ScoreEntry entry = entries.compute(entity, (_, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withNumberFormat(numberFormat)
        );
        sendUpdate(entity, entry);
    }

    @Override
    public void updateEntry(String entity, ScoreEntry entry) {
        entries.put(entity, entry);
        sendUpdate(entity, entry);
    }

    @Override
    public void removeEntry(String entity) {
        if (entries.remove(entity) == null) return;
        sendGroupedPacket(new ResetScorePacket(entity, objectiveName));
    }

    @Override
    public void sendUpdate(String entity, ScoreEntry entry) {
        sendGroupedPacket(entry.getUpdateScorePacket(entity, objectiveName));
    }

    public void sendObjectiveUpdate() {
        sendGroupedPacket(getUpdateObjectivePacket());
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        return Scoreboard.super.getPlayers();
    }
}