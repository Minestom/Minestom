package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A holder of a boss bar. This class is not intended for public use, instead you should
 * use {@link BossBarManager} to manage boss bars for players.
 */
final class BossBarHolder implements Viewable {
    final UUID uuid = UUID.randomUUID();
    final Set<Player> players = new CopyOnWriteArraySet<>();
    final BossBar bar;

    BossBarHolder(BossBar bar) {
        this.bar = bar;
    }

    BossBarPacket createRemovePacket() {
        return new BossBarPacket(uuid, new BossBarPacket.RemoveAction());
    }

    BossBarPacket createAddPacket() {
        return new BossBarPacket(uuid, new BossBarPacket.AddAction(bar));
    }

    BossBarPacket createPercentUpdate(float newPercent) {
        return new BossBarPacket(uuid, new BossBarPacket.UpdateHealthAction(newPercent));
    }

    BossBarPacket createColorUpdate(BossBar.Color color) {
        return new BossBarPacket(uuid, new BossBarPacket.UpdateStyleAction(color, bar.overlay()));
    }

    BossBarPacket createTitleUpdate(Component title) {
        return new BossBarPacket(uuid, new BossBarPacket.UpdateTitleAction(title));
    }

    BossBarPacket createFlagsUpdate() {
        return createFlagsUpdate(bar.flags());
    }

    BossBarPacket createFlagsUpdate(Set<BossBar.Flag> newFlags) {
        return new BossBarPacket(uuid, new BossBarPacket.UpdateFlagsAction(AdventurePacketConvertor.getBossBarFlagValue(newFlags)));
    }

    BossBarPacket createOverlayUpdate(BossBar.Overlay overlay) {
        return new BossBarPacket(uuid, new BossBarPacket.UpdateStyleAction(bar.color(), overlay));
    }

    @Override
    public boolean addViewer(Player player) {
        return this.players.add(player);
    }

    @Override
    public boolean removeViewer(Player player) {
        return this.players.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(this.players);
    }
}
