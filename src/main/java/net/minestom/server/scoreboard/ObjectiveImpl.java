package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

final class ObjectiveImpl implements Objective {
    private final String name;
    private volatile Component displayName;
    private volatile RenderType renderType = RenderType.INTEGER;
    private volatile @Nullable NumberFormat defaultNumberFormat;

    private final Map<String, ScoreEntry> entries = new ConcurrentHashMap<>();
    private final Map<String, ScoreEntry> unmodifiableEntries = Collections.unmodifiableMap(entries);
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);

    ObjectiveImpl(String name, Component displayName) {
        this.name = Objects.requireNonNull(name);
        this.displayName = Objects.requireNonNull(displayName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public synchronized void setDisplayName(Component displayName) {
        this.displayName = Objects.requireNonNull(displayName);
        sendGroupedPacket(getUpdateObjectivePacket());
    }

    @Override
    public RenderType getRenderType() {
        return renderType;
    }

    @Override
    public synchronized void setRenderType(RenderType renderType) {
        this.renderType = Objects.requireNonNull(renderType);
        sendGroupedPacket(getUpdateObjectivePacket());
    }

    @Override
    public @Nullable NumberFormat getDefaultNumberFormat() {
        return defaultNumberFormat;
    }

    @Override
    public synchronized void setDefaultNumberFormat(@Nullable NumberFormat numberFormat) {
        this.defaultNumberFormat = numberFormat;
        sendGroupedPacket(getUpdateObjectivePacket());
    }

    @Override
    public @Nullable ScoreEntry getEntry(String holder) {
        return entries.get(holder);
    }

    @Override
    public Map<String, ScoreEntry> getEntries() {
        return unmodifiableEntries;
    }

    @Override
    public synchronized void updateScore(String holder, int score) {
        final ScoreEntry entry = entries.compute(holder, (key, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withScore(score));
        sendGroupedPacket(getUpdateScorePacket(holder, entry));
    }

    @Override
    public synchronized void updateDisplayName(String holder, @Nullable Component displayName) {
        final ScoreEntry entry = entries.compute(holder, (key, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withDisplayName(displayName));
        sendGroupedPacket(getUpdateScorePacket(holder, entry));
    }

    @Override
    public synchronized void updateNumberFormat(String holder, @Nullable NumberFormat numberFormat) {
        final ScoreEntry entry = entries.compute(holder, (key, current) ->
                (current != null ? current : ScoreEntry.DEFAULT).withNumberFormat(numberFormat));
        sendGroupedPacket(getUpdateScorePacket(holder, entry));
    }

    @Override
    public synchronized void updateEntry(String holder, ScoreEntry entry) {
        entries.put(holder, entry);
        sendGroupedPacket(getUpdateScorePacket(holder, entry));
    }

    @Override
    public synchronized void removeEntry(String holder) {
        if (entries.remove(holder) == null) return;
        sendGroupedPacket(new ResetScorePacket(holder, name));
    }

    @Override
    public boolean isViewer(Player player) {
        return viewers.contains(player);
    }

    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    @Override
    public synchronized void updateNewViewer(Player player) {
        if (!viewers.add(player)) return;
        player.sendPacket(getCreationObjectivePacket());
        entries.forEach((holder, entry) -> player.sendPacket(getUpdateScorePacket(holder, entry)));
    }

    @Override
    public synchronized void updateOldViewer(Player player) {
        if (!viewers.remove(player)) return;
        player.sendPacket(getDestructionObjectivePacket());
    }

    private ScoreboardObjectivePacket getCreationObjectivePacket() {
        return new ScoreboardObjectivePacket(name, (byte) 0, displayName, renderType, defaultNumberFormat);
    }

    private ScoreboardObjectivePacket getUpdateObjectivePacket() {
        return new ScoreboardObjectivePacket(name, (byte) 2, displayName, renderType, defaultNumberFormat);
    }

    private ScoreboardObjectivePacket getDestructionObjectivePacket() {
        return new ScoreboardObjectivePacket(name, (byte) 1, null, null, null);
    }

    private UpdateScorePacket getUpdateScorePacket(String holder, ScoreEntry entry) {
        return new UpdateScorePacket(holder, name, entry.score(), entry.displayName(), entry.numberFormat());
    }
}
