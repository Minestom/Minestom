package net.minestom.server.adventure;

import com.google.common.collect.MapMaker;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Handler;

import static net.minestom.server.network.packet.server.play.BossBarPacket.Action.*;

/**
 * Manages all boss bars known to this Minestom instance. This implementation is heavily
 * based on <a href="https://github.com/VelocityPowered/Velocity">Velocity</a>'s
 * boss bar management system.
 */
public class BossBarManager implements BossBar.Listener {
    private final Map<BossBar, Holder> bars;

    /**
     * Creates a new boss bar manager.
     */
    public BossBarManager() {
        this.bars = new MapMaker().weakKeys().makeMap();

        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerDisconnectEvent.class, this::onDisconnect);
    }

    /**
     * Adds the specified player to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param player the intended viewer
     * @param bar the boss bar to show
     */
    public void addBossBar(@NotNull Player player, @NotNull BossBar bar) {
        Holder holder = this.getOrCreateHandler(bar);

        if (holder.players.add(player.getUuid())) {
            player.getPlayerConnection().sendPacket(holder.createAddPacket());
        }
    }
    /**
     * Removes the specified player from the boss bar's viewers and despawns the boss bar.
     *
     * @param player the intended viewer
     * @param bar the boss bar to hide
     */

    public void removeBossBar(@NotNull Player player, @NotNull BossBar bar) {
        Holder holder = this.getOrCreateHandler(bar);

        if (holder.players.remove(player.getUuid())) {
            player.getPlayerConnection().sendPacket(holder.createRemovePacket());
        }
    }

    @Override
    public void bossBarNameChanged(@NonNull BossBar bar, @NonNull Component oldName, @NonNull Component newName) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createTitleUpdate(newName), holder.players);
    }

    @Override
    public void bossBarProgressChanged(@NonNull BossBar bar, float oldProgress, float newProgress) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createPercentUpdate(newProgress), holder.players);
    }

    @Override
    public void bossBarColorChanged(@NonNull BossBar bar, @NonNull Color oldColor, @NonNull Color newColor) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createColorUpdate(newColor), holder.players);
    }

    @Override
    public void bossBarOverlayChanged(@NonNull BossBar bar, BossBar.@NonNull Overlay oldOverlay, BossBar.@NonNull Overlay newOverlay) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createOverlayUpdate(newOverlay), holder.players);
    }

    @Override
    public void bossBarFlagsChanged(@NonNull BossBar bar, @NonNull Set<BossBar.Flag> flagsAdded, @NonNull Set<BossBar.Flag> flagsRemoved) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createFlagsUpdate(), holder.players);
    }

    /**
     * Sends the packet to all players in the set, removing them if they no longer exist
     * in the connection manager.
     *
     * @param packet the packet
     * @param players the players
     */
    private void updatePlayers(BossBarPacket packet, Set<UUID> players) {
        Iterator<UUID> iterator = players.iterator();

        while (iterator.hasNext()) {
            Player player = MinecraftServer.getConnectionManager().getPlayer(iterator.next());

            if (player == null) {
                iterator.remove();
            } else {
                player.getPlayerConnection().sendPacket(packet);
            }
        }
    }

    /**
     * Gets or creates a handler for this bar.
     *
     * @param bar the bar
     *
     * @return the handler
     */
    private @NotNull Holder getOrCreateHandler(@NotNull BossBar bar) {
        Holder holder = this.bars.computeIfAbsent(bar, Holder::new);

        if (!holder.registered) {
            bar.addListener(this);
            holder.registered = true;
        }

        return holder;
    }

    /**
     * Called when a player disconnects. This removes the player from any boss bars they
     * may be subscribed to.
     *
     * @param event the event
     */
    private void onDisconnect(@NotNull PlayerDisconnectEvent event) {
        for (Holder holder : this.bars.values()) {
            holder.players.remove(event.getPlayer().getUuid());
        }
    }

    private static class Holder {
        protected final UUID uuid;
        protected final BossBar bar;
        protected final Set<UUID> players;
        protected boolean registered;

        Holder(@NotNull BossBar bar) {
            this.uuid = UUID.randomUUID();
            this.bar = bar;
            this.players = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
            this.registered = false;
        }

        BossBarPacket createRemovePacket() {
            return this.createGenericPacket(REMOVE, packet -> {});
        }

        BossBarPacket createAddPacket() {
            return this.createGenericPacket(ADD, packet -> {
                packet.title = GsonComponentSerializer.gson().serialize(bar.name());
                packet.color = bar.color().ordinal();
                packet.division = bar.overlay().ordinal();
                packet.health = bar.progress();
                packet.flags = serializeFlags(bar.flags());
            });
        }

        BossBarPacket createPercentUpdate(float newPercent) {
            return this.createGenericPacket(UPDATE_HEALTH, packet -> packet.health = newPercent);
        }

        BossBarPacket createColorUpdate(@NotNull Color color) {
            return this.createGenericPacket(UPDATE_STYLE, packet -> {
                packet.color = color.ordinal();
                packet.division = bar.overlay().ordinal();
            });
        }

        BossBarPacket createTitleUpdate(@NotNull Component title) {
            return this.createGenericPacket(UPDATE_TITLE, packet -> packet.title = GsonComponentSerializer.gson().serialize(title));
        }

        BossBarPacket createFlagsUpdate() {
            return createFlagsUpdate(bar.flags());
        }

        BossBarPacket createFlagsUpdate(@NotNull Set<Flag> newFlags) {
            return this.createGenericPacket(UPDATE_FLAGS, packet -> packet.flags = serializeFlags(bar.flags()));
        }

        BossBarPacket createOverlayUpdate(@NotNull Overlay overlay) {
            return this.createGenericPacket(UPDATE_STYLE, packet -> {
                packet.division = overlay.ordinal();
                packet.color = bar.color().ordinal();
            });
        }

        BossBarPacket createGenericPacket(@NotNull BossBarPacket.Action action, @NotNull Consumer<BossBarPacket> consumer) {
            BossBarPacket packet = new BossBarPacket();
            packet.uuid = this.uuid;
            packet.action = action;
            consumer.accept(packet);
            return packet;
        }

        private static byte serializeFlags(@NotNull Set<Flag> flags) {
            byte val = 0x0;
            for (Flag flag : flags) {
                val |= flag.ordinal();
            }
            return val;
        }
    }
}
