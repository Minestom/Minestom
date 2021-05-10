package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static net.minestom.server.network.packet.server.play.BossBarPacket.Action.*;

/**
 * A holder of a boss bar. This class is not intended for public use, instead you should
 * use {@link BossBarManager} to manage boss bars for players.
 */
final class BossBarHolder implements Viewable {

    protected final UUID uuid = UUID.randomUUID();
    protected final Set<Player> players = new CopyOnWriteArraySet<>();
    protected final BossBar bar;

    BossBarHolder(@NotNull BossBar bar) {
        this.bar = bar;
    }

    @NotNull BossBarPacket createRemovePacket() {
        return this.createGenericPacket(REMOVE, packet -> { });
    }

    @NotNull BossBarPacket createAddPacket() {
        return this.createGenericPacket(ADD, packet -> {
            packet.title = bar.name();
            packet.color = bar.color();
            packet.overlay = bar.overlay();
            packet.health = bar.progress();
            packet.flags = AdventurePacketConvertor.getBossBarFlagValue(bar.flags());
        });
    }

    @NotNull BossBarPacket createPercentUpdate(float newPercent) {
        return this.createGenericPacket(UPDATE_HEALTH, packet -> packet.health = newPercent);
    }

    @NotNull BossBarPacket createColorUpdate(@NotNull BossBar.Color color) {
        return this.createGenericPacket(UPDATE_STYLE, packet -> {
            packet.color = color;
            packet.overlay = bar.overlay();
        });
    }

    @NotNull BossBarPacket createTitleUpdate(@NotNull Component title) {
        return this.createGenericPacket(UPDATE_TITLE, packet -> packet.title = title);
    }

    @NotNull BossBarPacket createFlagsUpdate() {
        return createFlagsUpdate(bar.flags());
    }

    @NotNull BossBarPacket createFlagsUpdate(@NotNull Set<BossBar.Flag> newFlags) {
        return this.createGenericPacket(UPDATE_FLAGS, packet -> packet.flags = AdventurePacketConvertor.getBossBarFlagValue(newFlags));
    }

    @NotNull BossBarPacket createOverlayUpdate(@NotNull BossBar.Overlay overlay) {
        return this.createGenericPacket(UPDATE_STYLE, packet -> {
            packet.overlay = overlay;
            packet.color = bar.color();
        });
    }

    private @NotNull BossBarPacket createGenericPacket(@NotNull BossBarPacket.Action action, @NotNull Consumer<BossBarPacket> consumer) {
        BossBarPacket packet = new BossBarPacket();
        packet.uuid = this.uuid;
        packet.action = action;
        consumer.accept(packet);
        return packet;
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        return this.players.add(player);
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        return this.players.remove(player);
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return Collections.unmodifiableSet(this.players);
    }
}
