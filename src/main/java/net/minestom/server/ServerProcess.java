package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.data.DataManager;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

@ApiStatus.NonExtendable
public interface ServerProcess {
    static @NotNull ServerProcess newProcess() throws Exception {
        return new ServerProcessImpl();
    }

    @NotNull ConnectionManager connection();

    @NotNull InstanceManager instance();

    @NotNull BlockManager block();

    @NotNull CommandManager command();

    @NotNull RecipeManager recipe();

    @NotNull StorageManager storage();

    @NotNull DataManager data();

    @NotNull TeamManager team();

    @NotNull GlobalEventHandler eventHandler();

    @NotNull SchedulerManager scheduler();

    @NotNull BenchmarkManager benchmark();

    @NotNull DimensionTypeManager dimension();

    @NotNull BiomeManager biome();

    @NotNull AdvancementManager advancement();

    @NotNull BossBarManager bossBar();

    @NotNull ExtensionManager extension();

    @NotNull TagManager tag();

    @NotNull ExceptionManager exception();

    @NotNull PacketListenerManager packetListener();

    @NotNull PacketProcessor packetProcessor();

    @NotNull Server server();

    @NotNull ThreadDispatcher dispatcher();

    @NotNull Ticker ticker();

    void start(@NotNull SocketAddress socketAddress);

    void stop();

    boolean isAlive();

    @ApiStatus.NonExtendable
    interface Ticker extends Tickable {
    }
}
