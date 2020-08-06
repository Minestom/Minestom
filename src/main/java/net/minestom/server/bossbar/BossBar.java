package net.minestom.server.bossbar;

import net.minestom.server.Viewable;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represent a bossbar which can be showed to any player {@link #addViewer(Player)}
 */
public class BossBar implements Viewable {

    private UUID uuid = UUID.randomUUID();
    private Set<Player> viewers = new CopyOnWriteArraySet<>();

    private ColoredText title;
    private float progress;
    private BarColor color;
    private BarDivision division;
    private byte flags;

    public BossBar(ColoredText title, BarColor color, BarDivision division) {
        this.title = title;
        this.color = color;
        this.division = division;
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = this.viewers.add(player);
        if (result) {
            addToPlayer(player);
        }
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);
        if (result) {
            removeToPlayer(player);
        }
        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    /**
     * Get the bossbar title
     *
     * @return the current title of the bossbar
     */
    public ColoredText getTitle() {
        return title;
    }

    /**
     * Change the bossbar title
     *
     * @param title the new title of the bossbar
     */
    public void setTitle(ColoredText title) {
        this.title = title;
    }

    /**
     * Get the bossbar progress
     *
     * @return the current progress of the bossbar
     */
    public float getProgress() {
        return progress;
    }

    /**
     * Change the bossbar progress
     *
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
     * Get the bossbar color
     *
     * @return the current bossbar color
     */
    public BarColor getColor() {
        return color;
    }

    /**
     * Change the bossbar color
     *
     * @param color the new color of the bossbar
     */
    public void setColor(BarColor color) {
        this.color = color;
        updateStyle();
    }

    /**
     * Get the bossbar division
     *
     * @return the current bossbar division
     */
    public BarDivision getDivision() {
        return division;
    }

    /**
     * Change the bossbar division
     *
     * @param division the new bossbar division count
     */
    public void setDivision(BarDivision division) {
        this.division = division;
        updateStyle();
    }

    /**
     * Get the bossbar flags
     *
     * @return the flags
     */
    public byte getFlags() {
        return flags;
    }

    /**
     * Set the bossbar flags
     *
     * @param flags the bossbar flags
     * @see <a href="https://wiki.vg/Protocol#Boss_Bar">Boss bar packet</a>
     */
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    /**
     * Delete the boss bar and remove all of its viewers
     */
    public void delete() {
        BossBarPacket bossBarPacket = new BossBarPacket();
        bossBarPacket.uuid = uuid;
        bossBarPacket.action = BossBarPacket.Action.REMOVE;
        sendPacketToViewers(bossBarPacket);
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
