package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final BossBarListener listener = new BossBarListener(this);
    private final Map<UUID, Set<BossBarHolder>> playerBars = new ConcurrentHashMap<>();
    final Map<BossBar, BossBarHolder> bars = new ConcurrentHashMap<>();

    /**
     * Creates a new boss bar manager.
     *
     * @see MinecraftServer#getBossBarManager()
     */
    public BossBarManager() {
    }

    /**
     * Adds the specified player to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param player the intended viewer
     * @param bar    the boss bar to show
     */
    public void addBossBar(@NotNull Player player, @NotNull BossBar bar) {
        BossBarHolder holder = this.getOrCreateHandler(bar);
        if (holder.addViewer(player)) {
            player.getPlayerConnection().sendPacket(holder.createAddPacket());
            this.playerBars.computeIfAbsent(player.getUuid(), uuid -> new HashSet<>()).add(holder);
        }
    }

    /**
     * Removes the specified player from the boss bar's viewers and despawns the boss bar.
     *
     * @param player the intended viewer
     * @param bar    the boss bar to hide
     */
    public void removeBossBar(@NotNull Player player, @NotNull BossBar bar) {
        BossBarHolder holder = this.bars.get(bar);
        if (holder != null && holder.removeViewer(player)) {
            player.getPlayerConnection().sendPacket(holder.createRemovePacket());
            this.removePlayer(player, holder);
        }
    }

    /**
     * Adds the specified players to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param players the players
     * @param bar     the boss bar
     */
    public void addBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        BossBarHolder holder = this.getOrCreateHandler(bar);
        Collection<Player> addedPlayers = players.stream().filter(holder::addViewer).toList();
        if (!addedPlayers.isEmpty()) {
            PacketUtils.sendGroupedPacket(addedPlayers, holder.createAddPacket());
        }
    }

    /**
     * Removes the specified players from the boss bar's viewers and despawns the boss bar.
     *
     * @param players the intended viewers
     * @param bar     the boss bar to hide
     */
    public void removeBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        BossBarHolder holder = this.bars.get(bar);
        if (holder != null) {
            Collection<Player> removedPlayers = players.stream().filter(holder::removeViewer).toList();
            if (!removedPlayers.isEmpty()) {
                PacketUtils.sendGroupedPacket(removedPlayers, holder.createRemovePacket());
            }
        }
    }

    /**
     * Completely destroys a boss bar, removing it from all players.
     *
     * @param bossBar the boss bar
     */
    public void destroyBossBar(@NotNull BossBar bossBar) {
        BossBarHolder holder = this.bars.remove(bossBar);
        if (holder != null) {
            PacketUtils.sendGroupedPacket(holder.players, holder.createRemovePacket());
            for (Player player : holder.players) {
                this.removePlayer(player, holder);
            }
        }
    }

    /**
     * Removes a player from all of their boss bars. Note that this method does not
     * send any removal packets to the player. It is meant to be used when a player is
     * disconnecting from the server.
     *
     * @param player the player
     */
    public void removeAllBossBars(@NotNull Player player) {
        Set<BossBarHolder> holders = this.playerBars.remove(player.getUuid());
        if (holders != null) {
            for (BossBarHolder holder : holders) {
                holder.removeViewer(player);
            }
        }
    }

    /**
     * Gets a collection of all boss bars currently visible to a given player.
     *
     * @param player the player
     * @return the boss bars
     */
    public @NotNull Collection<BossBar> getPlayerBossBars(@NotNull Player player) {
        Collection<BossBarHolder> holders = this.playerBars.get(player.getUuid());
        return holders != null ?
                holders.stream().map(holder -> holder.bar).toList() : Collections.emptyList();
    }

    /**
     * Gets all the players for whom the given boss bar is currently visible.
     *
     * @param bossBar the boss bar
     * @return the players
     */
    public @NotNull Collection<Player> getBossBarViewers(@NotNull BossBar bossBar) {
        BossBarHolder holder = this.bars.get(bossBar);
        return holder != null ?
                Collections.unmodifiableCollection(holder.players) : Collections.emptyList();
    }

    /**
     * Gets or creates a handler for this bar.
     *
     * @param bar the bar
     * @return the handler
     */
    private @NotNull BossBarHolder getOrCreateHandler(@NotNull BossBar bar) {
        return this.bars.computeIfAbsent(bar, bossBar -> {
            BossBarHolder holder = new BossBarHolder(bossBar);
            bossBar.addListener(this.listener);
            return holder;
        });
    }

    private void removePlayer(Player player, BossBarHolder holder) {
        Set<BossBarHolder> holders = this.playerBars.get(player.getUuid());
        if (holders != null) {
            holders.remove(holder);
            if (holders.isEmpty()) {
                this.playerBars.remove(player.getUuid());
            }
        }
    }
}
