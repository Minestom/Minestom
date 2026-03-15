package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A scoreboard that can be viewed but does not store score data.
 */
class BaseScoreboard implements Scoreboard {

    protected final String objectiveName;
    protected @Nullable Component displayName;
    protected Position position;
    protected ScoreboardObjectivePacket.Type displayType = ScoreboardObjectivePacket.Type.INTEGER;
    protected @Nullable NumberFormat defaultNumberFormat;

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);


    BaseScoreboard(String objectiveName, Position position) {
        this.objectiveName = objectiveName;
        this.position = position;
    }

    @Override
    public boolean addViewer(Player player) {
        boolean added = viewers.add(player);
        if (!added) return false;
        player.sendPackets(getCreationObjectivePacket(), getDisplayScoreboardPacket(position.asByte()));
        return true;
    }

    @Override
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
        sendUpdate(entity, score, null);
    }

    @Override
    public void updateScore(String entity, int score, @Nullable NumberFormat numberFormat) {
        sendUpdate(entity, score, numberFormat);
    }

    @Override
    public void updateNumberFormat(String entity, NumberFormat numberFormat) {
        sendUpdate(entity, 0, numberFormat);
    }

    public void removeScore(String entity) {
        sendPacketToViewers(new ResetScorePacket(entity, objectiveName));
    }

    public void sendUpdate(String entity, int score, @Nullable NumberFormat numberFormat) {
        sendPacketToViewers(new UpdateScorePacket(entity, objectiveName, score, displayName, numberFormat));
    }

    public void sendObjectiveUpdate() {
        sendPacketToViewers(getUpdateObjectivePacket());
    }

    @Override
    public Collection<Player> getPlayers() {
        return Scoreboard.super.getPlayers();
    }
}
