package net.minestom.demo;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.demo.commands.DisplayCommand;
import net.minestom.demo.commands.DisplaynameCommand;
import net.minestom.demo.commands.PerPlayerCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.lan.OpenToLANConfig;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;

import java.time.Duration;

public class MainPerPlayerDemo {

    public static final ComponentLogger LOGGER = ComponentLogger.logger(MainPerPlayerDemo.class);
    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        MojangAuth.init();

        MinecraftServer.getCommandManager().register(new PerPlayerCommand());
        MinecraftServer.getCommandManager().register(new DisplayCommand());
        MinecraftServer.getCommandManager().register(new DisplaynameCommand());

        MinecraftServer.getConnectionManager().setPlayerProvider(PerPlayer::new);
        DimensionType dimension = DimensionType.builder(NamespaceID.from("freddi:fulllight")).ambientLight(2.0f).skylightEnabled(true).build();
        MinecraftServer.getDimensionTypeManager().addDimension(dimension);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(dimension);

        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.BLUE_WOOL));


        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final PerPlayer player = (PerPlayer) event.getPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setPermissionLevel(2);
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
        

        globalEventHandler.addListener(PlayerCommandEvent.class, event -> {
            LOGGER.info(event.getPlayer().getUsername() + "(" + event.getPlayer().getUuid() + "): /" + event.getCommand());
        });
        globalEventHandler.addListener(PlayerChatEvent.class,event -> {
            PerPlayer player = (PerPlayer) event.getPlayer();
            LOGGER.info(event.getPlayer().getUsername() + "(" + event.getPlayer().getUuid() + "): " + event.getMessage());

        });

        // Start the server a free Port
        OpenToLAN.open(new OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)));
        minecraftServer.start("0.0.0.0", 25565);

    }

}
