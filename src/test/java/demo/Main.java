package demo;

import demo.blocks.BurningTorchBlock;
import demo.blocks.CustomBlockSample;
import demo.blocks.UpdatableBlockDemo;
import demo.commands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.server.ServerListPingEvent;
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
        commandManager.register(new GiveCommand());
        commandManager.register(new ParticleCommand());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));


        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        MinecraftServer.getBenchmarkManager().enable(new UpdateOption(10 * 1000, TimeUnit.MILLISECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night")).schedule();

        MinecraftServer.getGlobalEventHandler().addEventCallback(ServerListPingEvent.class, event -> {
            ResponseData responseData = event.getResponseData();
            responseData.setMaxPlayer(0);
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.addPlayer("The first line is separated from the others", UUID.randomUUID());
            responseData.addPlayer("Could be a name, or a message", UUID.randomUUID());
            responseData.addPlayer("IP test: " + event.getConnection().getRemoteAddress().toString(), UUID.randomUUID());
            responseData.addPlayer("Use " + (char)0x00a7 + "7section characters", UUID.randomUUID());
            responseData.addPlayer((char)0x00a7 + "7" + (char)0x00a7 + "ofor formatting" + (char)0x00a7 + "r: (" + (char)0x00a7 + "6char" + (char)0x00a7 + "r)" + (char)0x00a7 + "90x00a7", UUID.randomUUID());

            responseData.addPlayer("Connection Info:");
            String ip = event.getConnection().getServerAddress();
            responseData.addPlayer((char)0x00a7 + "8-  " + (char)0x00a7 +"7IP: " + (char)0x00a7 + "e" + (ip != null ? ip : "???"));
            responseData.addPlayer((char)0x00a7 + "8-  " + (char)0x00a7 +"7PORT: " + (char)0x00a7 + "e" + event.getConnection().getServerPort());
            responseData.addPlayer((char)0x00a7 + "8-  " + (char)0x00a7 +"7VERSION: " + (char)0x00a7 + "e" + event.getConnection().getProtocolVersion());

            // Check if client supports RGB color
            if (event.getConnection().getProtocolVersion() >= 713) { // Snapshot 20w17a
                responseData.setDescription(Component.text("You can do ")
                        .append(Component.text("RGB", TextColor.color(0x66b3ff)))
                        .append(Component.text(" color here")));
            } else {
                responseData.setDescription(Component.text("You can do ")
                        .append(Component.text("RGB", NamedTextColor.nearestTo(TextColor.color(0x66b3ff))))
                        .append(Component.text(" color here,"))
                        .append(Component.newline())
                        .append(Component.text("if you are on 1.16 or up"))
                );
            }




        });

        PlayerInit.init();

        OptifineSupport.enable();

        //VelocityProxy.enable("rBeJJ79W4MVU");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25565);
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }

}
