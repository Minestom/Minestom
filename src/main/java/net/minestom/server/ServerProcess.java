package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.Registries;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.SchedulerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

@ApiStatus.NonExtendable
public interface ServerProcess extends Registries, Snapshotable {
    /**
     * Handles incoming connections/players.
     */
    @NotNull ConnectionManager connection();

    /**
     * Handles registered instances.
     */
    @NotNull InstanceManager instance();

    /**
     * Handles {@link net.minestom.server.instance.block.BlockHandler block handlers}
     * and {@link BlockPlacementRule placement rules}.
     */
    @NotNull BlockManager block();

    /**
     * Handles registered commands.
     */
    @NotNull CommandManager command();

    /**
     * Handles registered recipes shown to clients.
     */
    @NotNull RecipeManager recipe();

    /**
     * Handles registered teams.
     */
    @NotNull TeamManager team();

    /**
     * Gets the global event handler.
     * <p>
     * Used to register event callback at a global scale.
     */
    @NotNull GlobalEventHandler eventHandler();

    /**
     * Main scheduler ticked at the server rate.
     */
    @NotNull SchedulerManager scheduler();

    @NotNull BenchmarkManager benchmark();

    /**
     * Handles registered advancements.
     */
    @NotNull AdvancementManager advancement();

    /**
     * Handles registered boss bars.
     */
    @NotNull BossBarManager bossBar();

    /**
     * Handles all thrown exceptions from the server.
     */
    @NotNull ExceptionManager exception();

    /**
     * Handles incoming packets.
     */
    @NotNull PacketListenerManager packetListener();

    /**
     * Gets the object handling the client packets parsing.
     * <p>
     * Can be used if you want to convert a buffer to a client packet object.
     */
    @NotNull PacketParser<ClientPacket> packetParser();

    /**
     * Exposed socket server.
     */
    @NotNull Server server();

    /**
     * Dispatcher for tickable game objects.
     */
    @NotNull ThreadDispatcher<Chunk, Entity> dispatcher();

    /**
     * Handles the server ticks.
     */
    @NotNull Ticker ticker();

    void start(@NotNull SocketAddress socketAddress);

    void stop();

    boolean isAlive();

    @ApiStatus.NonExtendable
    interface Ticker {
        void tick(long nanoTime);
    }
}
