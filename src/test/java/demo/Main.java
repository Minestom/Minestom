package demo;

import demo.blocks.BurningTorchBlock;
import demo.blocks.CustomBlockSample;
import demo.blocks.UpdatableBlockDemo;
import demo.commands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.server.StatusRequestEvent;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.storage.systems.FileStorageSystem;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

import java.util.UUID;


public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        // MinecraftServer.setShouldProcessNettyErrors(true);

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerCustomBlock(new CustomBlockSample());
        blockManager.registerCustomBlock(new UpdatableBlockDemo());
        blockManager.registerCustomBlock(new BurningTorchBlock());

        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TestCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new EntitySelectorCommand());
        commandManager.register(new HealthCommand());
        commandManager.register(new LegacyCommand());
        commandManager.register(new DimensionCommand());
        commandManager.register(new ShutdownCommand());
        commandManager.register(new TeleportCommand());
        commandManager.register(new PlayersCommand());
        commandManager.register(new PotionCommand());
        commandManager.register(new TitleCommand());
        commandManager.register(new BookCommand());
        commandManager.register(new ShootCommand());
        commandManager.register(new HorseCommand());
        commandManager.register(new EchoCommand());
        commandManager.register(new SummonCommand());
        commandManager.register(new RemoveCommand());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));


        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        MinecraftServer.getBenchmarkManager().enable(new UpdateOption(10 * 1000, TimeUnit.MILLISECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night")).schedule();

        MinecraftServer.getGlobalEventHandler().addEventCallback(StatusRequestEvent.class, event -> {
            ResponseData responseData = event.getResponseData();
            responseData.addPlayer("IP test: " + event.getConnection().getRemoteAddress().toString(), UUID.randomUUID());
            responseData.addPlayer("Use " + (char)0x00a7 + "7section characters", UUID.randomUUID());
            responseData.addPlayer((char)0x00a7 + "7" + (char)0x00a7 + "ofor formatting" + (char)0x00a7 + "r: (" + (char)0x00a7 + "6char" + (char)0x00a7 + "r)" + (char)0x00a7 + "90x00a7", UUID.randomUUID());

            
        });

        PlayerInit.init();

        OptifineSupport.enable();

        //VelocityProxy.enable("rBeJJ79W4MVU");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25565, PlayerInit.getResponseDataConsumer());
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }

}
