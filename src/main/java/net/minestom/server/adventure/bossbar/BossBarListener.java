package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.PacketSendingUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

/**
 * A listener for boss bar updates. This class is not intended for public use and it is
 * automatically added to boss bars shown to players using the methods in
 * {@link Audience}, instead you should use {@link BossBarManager} to manage boss bars
 * for players.
 */
class BossBarListener implements BossBar.Listener {
    private final BossBarManager manager;

    /**
     * Creates a new boss bar listener.
     *
     * @param manager the manager instance
     */
    BossBarListener(BossBarManager manager) {
        this.manager = manager;
    }

    @Override
    public void bossBarNameChanged(@NotNull BossBar bar, @NotNull Component oldName, @NotNull Component newName) {
        this.doIfRegistered(bar, holder -> PacketSendingUtils.sendGroupedPacket(holder.players, holder.createTitleUpdate(newName)));
    }

    @Override
    public void bossBarProgressChanged(@NotNull BossBar bar, float oldProgress, float newProgress) {
        this.doIfRegistered(bar, holder -> PacketSendingUtils.sendGroupedPacket(holder.players, holder.createPercentUpdate(newProgress)));

    }

    @Override
    public void bossBarColorChanged(@NotNull BossBar bar, @NotNull BossBar.Color oldColor, @NotNull BossBar.Color newColor) {
        this.doIfRegistered(bar, holder -> PacketSendingUtils.sendGroupedPacket(holder.players, holder.createColorUpdate(newColor)));
    }

    @Override
    public void bossBarOverlayChanged(@NotNull BossBar bar, BossBar.@NotNull Overlay oldOverlay, BossBar.@NotNull Overlay newOverlay) {
        this.doIfRegistered(bar, holder -> PacketSendingUtils.sendGroupedPacket(holder.players, holder.createOverlayUpdate(newOverlay)));
    }

    @Override
    public void bossBarFlagsChanged(@NotNull BossBar bar, @NotNull Set<BossBar.Flag> flagsAdded, @NotNull Set<BossBar.Flag> flagsRemoved) {
        this.doIfRegistered(bar, holder -> PacketSendingUtils.sendGroupedPacket(holder.players, holder.createFlagsUpdate()));
    }

    private void doIfRegistered(@NotNull BossBar bar, @NotNull Consumer<BossBarHolder> consumer) {
        BossBarHolder holder = this.manager.bars.get(bar);
        if (holder != null) {
            consumer.accept(holder);
        }
    }
}
