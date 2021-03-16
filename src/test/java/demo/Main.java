package demo;

import demo.blocks.BurningTorchBlock;
import demo.blocks.CustomBlockSample;
import demo.blocks.UpdatableBlockDemo;
import demo.commands.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.item.Material;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.storage.systems.FileStorageSystem;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;


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

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage("unknown command"));


        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        MinecraftServer.getBenchmarkManager().enable(new UpdateOption(10 * 1000, TimeUnit.MILLISECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night")).schedule();

        PlayerInit.init();

        OptifineSupport.enable();

        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();
            String msg = event.getMessage();
            if (!msg.equals("a") && !msg.equals("b")) return;
            if (msg.equals("b")) {
                player.getPosition().add(0, 200,0);
                player.getInstance().setBlock(player.getPosition().toBlockPosition().add(0,-1,0), Block.GOLD_BLOCK);
                return;
            }
            player.getPosition().add(0, 200,0);
            EntityCreature livingEntity = new EntityCreature(EntityType.ZOMBIE);
            livingEntity.setInstance(event.getPlayer().getInstance(), event.getPlayer().getPosition());
            livingEntity.attack(player);
        });
        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerBlockInteractEvent.class, event -> {
            if (!event.getPlayer().getItemInMainHand().getMaterial().equals(Material.ZOMBIE_SPAWN_EGG)) return;
            EntityCreature livingEntity = new EntityCreature(EntityType.ZOMBIE);
            livingEntity.setInstance(event.getPlayer().getInstance(), event.getPlayer().getTargetBlockPosition(10).add(0,1,0).toPosition());
        });

        //VelocityProxy.enable("rBeJJ79W4MVU");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25565, PlayerInit.getResponseDataConsumer());
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }

}
