package fr.themode.minestom.bossbar;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.BossBarPacket;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class BossBar implements Viewable {

    private UUID uuid = UUID.randomUUID();
    private Set<Player> viewers = new CopyOnWriteArraySet<>();

    private String title;
    private float progress;
    private BarColor color;
    private BarDivision division;
    private byte flags;

    public BossBar(String title, BarColor color, BarDivision division) {
        this.title = title;
        this.color = color;
        this.division = division;
    }

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
        player.refreshAddBossbar(this);
        addToPlayer(player);
    }

    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
        player.refreshRemoveBossbar(this);
        removeToPlayer(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        updateProgress();
    }

    public BarColor getColor() {
        return color;
    }

    public void setColor(BarColor color) {
        this.color = color;
        updateStyle();
    }

    public BarDivision getDivision() {
        return division;
    }

    public void setDivision(BarDivision division) {
        this.division = division;
        updateStyle();
    }

    public void delete() {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.REMOVE;
        sendPacketToViewers(bossBarPacket);
        getViewers().forEach(player -> player.refreshRemoveBossbar(this));
    }

    private void addToPlayer(Player player) {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.ADD;
        bossBarPacket.title = title;
        bossBarPacket.health = progress;
        bossBarPacket.color = color;
        bossBarPacket.division = division;
        bossBarPacket.flags = flags;
        player.getPlayerConnection().sendPacket(bossBarPacket);
    }

    private void removeToPlayer(Player player) {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.REMOVE;
        player.getPlayerConnection().sendPacket(bossBarPacket);
    }

    private void updateTitle() {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.UPDATE_TITLE;
        bossBarPacket.title = title;
        sendPacketToViewers(bossBarPacket);
    }

    private void updateProgress() {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.UPDATE_HEALTH;
        bossBarPacket.health = progress;
        sendPacketToViewers(bossBarPacket);
    }

    private void updateStyle() {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.UPDATE_STYLE;
        bossBarPacket.color = color;
        sendPacketToViewers(bossBarPacket);
    }
}
