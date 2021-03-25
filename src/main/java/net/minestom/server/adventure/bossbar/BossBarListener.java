package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
     * @param manager the manager instance
     */
    BossBarListener(BossBarManager manager) {
        this.manager = manager;
    }

    @Override
    public void bossBarNameChanged(@NotNull BossBar bar, @NotNull Component oldName, @NotNull Component newName) {
        BossBarHolder holder = this.manager.bars.get(bar);
        this.manager.updatePlayers(holder.createTitleUpdate(newName), holder.players);
    }

    @Override
    public void bossBarProgressChanged(@NotNull BossBar bar, float oldProgress, float newProgress) {
        BossBarHolder holder = this.manager.bars.get(bar);
        this.manager.updatePlayers(holder.createPercentUpdate(newProgress), holder.players);
    }

    @Override
    public void bossBarColorChanged(@NotNull BossBar bar, @NotNull BossBar.Color oldColor, @NotNull BossBar.Color newColor) {
        BossBarHolder holder = this.manager.bars.get(bar);
        this.manager.updatePlayers(holder.createColorUpdate(newColor), holder.players);
    }

    @Override
    public void bossBarOverlayChanged(@NotNull BossBar bar, BossBar.@NotNull Overlay oldOverlay, BossBar.@NotNull Overlay newOverlay) {
        BossBarHolder holder = this.manager.bars.get(bar);
        this.manager.updatePlayers(holder.createOverlayUpdate(newOverlay), holder.players);
    }

    @Override
    public void bossBarFlagsChanged(@NotNull BossBar bar, @NotNull Set<BossBar.Flag> flagsAdded, @NotNull Set<BossBar.Flag> flagsRemoved) {
        BossBarHolder holder = this.manager.bars.get(bar);
        this.manager.updatePlayers(holder.createFlagsUpdate(), holder.players);
    }
}
