package demo;

import demo.blocks.BurningTorchBlock;
import demo.blocks.StoneBlock;
import demo.blocks.UpdatableBlockDemo;
import demo.commands.*;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.network.netty.NettyServer;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.storage.systems.FileStorageSystem;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;


public class Main {

    public static void main(String[] args) {
        //System.setProperty("io.netty.leakDetection.targetRecords", "10");
        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.setShouldProcessNettyErrors(false);

        final NettyServer nettyServer = MinecraftServer.getNettyServer();
        final GlobalChannelTrafficShapingHandler trafficHandler = nettyServer.getGlobalTrafficHandler();
        trafficHandler.setReadChannelLimit(500_000);
        trafficHandler.setReadChannelLimit(500_000);
        //trafficHandler.setReadLimit(500_000);
        //trafficHandler.setReadLimit(500_000);

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerCustomBlock(new StoneBlock());
        blockManager.registerCustomBlock(new UpdatableBlockDemo());
        blockManager.registerCustomBlock(new BurningTorchBlock());

        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TestCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new EntitySelectorCommand());
        commandManager.register(new HealthCommand());
        commandManager.register(new SimpleCommand());
        commandManager.register(new DimensionCommand());
        commandManager.register(new ShutdownCommand());
        commandManager.register(new TeleportCommand());
        commandManager.register(new PlayersCommand());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage("unknown command"));


        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        MinecraftServer.getBenchmarkManager().enable(new UpdateOption(10 * 1000, TimeUnit.MILLISECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night")).schedule();

        PlayerInit.init();

        OptifineSupport.enable();

        //VelocityProxy.enable("rBeJJ79W4MVU");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25565, PlayerInit.getResponseDataConsumer());
    }

}
