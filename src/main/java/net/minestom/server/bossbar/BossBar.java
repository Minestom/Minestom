package net.minestom.server.bossbar;

import net.minestom.server.Viewable;
import net.minestom.server.chat.Chat;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;

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
    public boolean addViewer(Player player) {
        boolean result = this.viewers.add(player);
        player.refreshAddBossbar(this);
        addToPlayer(player);
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        boolean result = this.viewers.remove(player);
        player.refreshRemoveBossbar(this);
        removeToPlayer(player);
        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    /**
     * @return the current title of the bossbar
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the new title of the bossbar
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the current progress of the bossbar
     */
    public float getProgress() {
        return progress;
    }

    /**
     * @param progress the new progress bar percentage
     * @throws IllegalArgumentException if {@code progress} is not between 0 and 1
     */
    public void setProgress(float progress) {
        Check.argCondition(!MathUtils.isBetween(progress, 0, 1),
                "BossBar progress percentage should be between 0 and 1");
        this.progress = progress;
        updateProgress();
    }

    /**
     * @return the current bossbar color
     */
    public BarColor getColor() {
        return color;
    }

    /**
     * @param color the new color of the bossbar
     */
    public void setColor(BarColor color) {
        this.color = color;
        updateStyle();
    }

    /**
     * @return the current bossbar division
     */
    public BarDivision getDivision() {
        return division;
    }

    /**
     * @param division the new bossbar division count
     */
    public void setDivision(BarDivision division) {
        this.division = division;
        updateStyle();
    }

    /**
     * Delete the boss bar and remove all of its viewers
     */
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
        bossBarPacket.title = Chat.fromLegacyText(title);
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
        bossBarPacket.title = Chat.fromLegacyText(title);
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
