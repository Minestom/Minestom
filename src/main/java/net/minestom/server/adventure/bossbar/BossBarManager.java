package net.minestom.server.adventure.bossbar;

import com.google.common.collect.MapMaker;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.network.packet.server.play.BossBarPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages all boss bars known to this Minestom instance. Although this class can be used
 * to show boss bars to players, it is preferable to use the boss bar methods in the
 * {@link Audience} class instead.
 *
 * <p>This implementation is heavily based on
 * <a href="https://github.com/VelocityPowered/Velocity">Velocity</a>'s boss bar
 * management system.</p>
 *
 * @see Audience#showBossBar(BossBar)
 * @see Audience#hideBossBar(BossBar)
 */
public class BossBarManager {
    private static final int CONCURRENCY_LEVEL = 4;

    private final BossBarListener listener;
    final Map<BossBar, BossBarHolder> bars;

    /**
     * Creates a new boss bar manager.
     */
    public BossBarManager() {
        this.listener = new BossBarListener(this);
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
        BossBarHolder holder = this.getOrCreateHandler(bar);

        if (holder.addViewer(player)) {
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
        BossBarHolder holder = this.getOrCreateHandler(bar);

        if (holder.removeViewer(player)) {
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
        BossBarHolder holder = this.getOrCreateHandler(bar);
        Collection<Player> addedPlayers = new ArrayList<>();

        for (Player player : players) {
            if (holder.addViewer(player)) {
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
        BossBarHolder holder = this.getOrCreateHandler(bar);
        Collection<Player> removedPlayers = new ArrayList<>();

        for (Player player : players) {
            if (holder.removeViewer(player)) {
                removedPlayers.add(player);
            }
        }

        if (!removedPlayers.isEmpty()) {
            PacketUtils.sendGroupedPacket(players, holder.createRemovePacket());
        }
    }

    /**
     * Sends the packet to all players in the set, removing them if they no longer exist
     * in the connection manager.
     *
     * @param packet the packet
     * @param uuids the players
     */
    void updatePlayers(BossBarPacket packet, Set<UUID> uuids) {
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
    private @NotNull BossBarHolder getOrCreateHandler(@NotNull BossBar bar) {
        BossBarHolder holder = this.bars.computeIfAbsent(bar, BossBarHolder::new);

        if (!holder.registered) {
            bar.addListener(this.listener);
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
        for (BossBarHolder holder : this.bars.values()) {
            holder.players.remove(event.getPlayer().getUuid());
        }
    }
}
