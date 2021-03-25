package net.minestom.server.adventure;

import com.google.common.collect.MapMaker;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minestom.server.network.packet.server.play.BossBarPacket.Action.*;

/**
 * Manages all boss bars known to this Minestom instance. This implementation is heavily
 * based on <a href="https://github.com/VelocityPowered/Velocity">Velocity</a>'s
 * boss bar management system.
 */
public class BossBarManager implements BossBar.Listener {
    private static final int CONCURRENCY_LEVEL = 4;

    private final Map<BossBar, Holder> bars;

    /**
     * Creates a new boss bar manager.
     */
    public BossBarManager() {
        this.bars = new MapMaker().concurrencyLevel(CONCURRENCY_LEVEL).weakKeys().makeMap();

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

    /**
     * Adds the specified players to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param players the players
     * @param bar the boss bar
     */
    public void addBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        Holder holder = this.getOrCreateHandler(bar);
        Collection<Player> addedPlayers = new ArrayList<>();

        for (Player player : players) {
            if (holder.players.add(player.getUuid())) {
                addedPlayers.add(player);
            }
        }

        if (!addedPlayers.isEmpty()) {
            PacketUtils.sendGroupedPacket(players, holder.createAddPacket());
        }
    }

    /**
     * Removes the specified players from the boss bar's viewers and despawns the boss bar.
     *
     * @param players the intended viewers
     * @param bar the boss bar to hide
     */
    public void removeBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        Holder holder = this.getOrCreateHandler(bar);
        Collection<Player> removedPlayers = new ArrayList<>();

        for (Player player : players) {
            if (holder.players.remove(player.getUuid())) {
                removedPlayers.add(player);
            }
        }

        if (!removedPlayers.isEmpty()) {
            PacketUtils.sendGroupedPacket(players, holder.createRemovePacket());
        }
    }

    @Override
    public void bossBarNameChanged(@NotNull BossBar bar, @NotNull Component oldName, @NotNull Component newName) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createTitleUpdate(newName), holder.players);
    }

    @Override
    public void bossBarProgressChanged(@NotNull BossBar bar, float oldProgress, float newProgress) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createPercentUpdate(newProgress), holder.players);
    }

    @Override
    public void bossBarColorChanged(@NotNull BossBar bar, @NotNull Color oldColor, @NotNull Color newColor) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createColorUpdate(newColor), holder.players);
    }

    @Override
    public void bossBarOverlayChanged(@NotNull BossBar bar, BossBar.@NotNull Overlay oldOverlay, BossBar.@NotNull Overlay newOverlay) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createOverlayUpdate(newOverlay), holder.players);
    }

    @Override
    public void bossBarFlagsChanged(@NotNull BossBar bar, @NotNull Set<BossBar.Flag> flagsAdded, @NotNull Set<BossBar.Flag> flagsRemoved) {
        Holder holder = this.bars.get(bar);
        this.updatePlayers(holder.createFlagsUpdate(), holder.players);
    }

    /**
     * Sends the packet to all players in the set, removing them if they no longer exist
     * in the connection manager.
     *
     * @param packet the packet
     * @param uuids the players
     */
    private void updatePlayers(BossBarPacket packet, Set<UUID> uuids) {
        Iterator<UUID> iterator = uuids.iterator();
        Collection<Player> players = new ArrayList<>();

        while (iterator.hasNext()) {
            Player player = MinecraftServer.getConnectionManager().getPlayer(iterator.next());

            if (player == null) {
                iterator.remove();
            } else {
                players.add(player);
            }
        }

        PacketUtils.sendGroupedPacket(players, packet);
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

        @NotNull BossBarPacket createRemovePacket() {
            return this.createGenericPacket(REMOVE, packet -> {});
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

        @NotNull BossBarPacket createColorUpdate(@NotNull Color color) {
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

        @NotNull BossBarPacket createFlagsUpdate(@NotNull Set<Flag> newFlags) {
            return this.createGenericPacket(UPDATE_FLAGS, packet -> packet.flags = AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

        @NotNull BossBarPacket createOverlayUpdate(@NotNull Overlay overlay) {
            return this.createGenericPacket(UPDATE_STYLE, packet -> {
                packet.overlay = overlay;
                packet.color = bar.color();
            });
        }

        @NotNull BossBarPacket createGenericPacket(@NotNull BossBarPacket.Action action, @NotNull Consumer<BossBarPacket> consumer) {
            BossBarPacket packet = new BossBarPacket();
            packet.uuid = this.uuid;
            packet.action = action;
            consumer.accept(packet);
            return packet;
        }
    }
}
