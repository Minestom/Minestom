package net.minestom.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.demo.block.SignHandler;
import net.minestom.demo.block.TestBlockHandler;
import net.minestom.demo.block.placement.BedPlacementRule;
import net.minestom.demo.block.placement.DripstonePlacementRule;
import net.minestom.demo.commands.*;
import net.minestom.demo.recipe.ShapelessRecipe;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.lan.OpenToLANConfig;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.ping.Status;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.utils.time.TimeUnit;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        System.setProperty("minestom.new-socket-write-lock", "true");
        MinecraftServer.setCompressionThreshold(0);

        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Offline());

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new DripstonePlacementRule());
        var beds = Block.values().stream().filter(block -> BlockEntityType.BED.equals(block.registry().blockEntityType())).toList();
        beds.forEach(block -> blockManager.registerBlockPlacementRule(new BedPlacementRule(block)));
        blockManager.registerHandler(TestBlockHandler.INSTANCE.getKey(), () -> TestBlockHandler.INSTANCE);

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TestCommand());
        commandManager.register(new EntitySelectorCommand());
        commandManager.register(new HealthCommand());
        commandManager.register(new LegacyCommand());
        commandManager.register(new DimensionCommand());
        commandManager.register(new ShutdownCommand());
        commandManager.register(new TeleportCommand());
        commandManager.register(new PlayersCommand());
        commandManager.register(new FindCommand());
        commandManager.register(new TitleCommand());
        commandManager.register(new BookCommand());
        commandManager.register(new ShootCommand());
        commandManager.register(new HorseCommand());
        commandManager.register(new EchoCommand());
        commandManager.register(new SummonCommand());
        commandManager.register(new RemoveCommand());
        commandManager.register(new GiveCommand());
        commandManager.register(new SetBlockCommand());
        commandManager.register(new AutoViewCommand());
        commandManager.register(new SaveCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new ExecuteCommand());
        commandManager.register(new RedirectTestCommand());
        commandManager.register(new DebugGridCommand());
        commandManager.register(new DisplayCommand());
        commandManager.register(new NotificationCommand());
        commandManager.register(new TestCommand2());
        commandManager.register(new ConfigCommand());
        commandManager.register(new SidebarCommand());
        commandManager.register(new SetEntityType());
        commandManager.register(new RelightCommand());
        commandManager.register(new KillCommand());
        commandManager.register(new WeatherCommand());
        commandManager.register(new PotionCommand());
        commandManager.register(new CookieCommand());
        commandManager.register(new WorldBorderCommand());
        commandManager.register(new TransferCommand());
        commandManager.register(new TestInstabreakCommand());
        commandManager.register(new AttributeCommand());
        commandManager.register(new PrimedTNTCommand());
        commandManager.register(new SleepCommand());
        commandManager.register(new ChickenCommand());
        commandManager.register(new MinecartCommand());
        commandManager.register(new BelowNameCommand());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));

        MinecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night"));

        RegistryTag<Block> tag = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:all_signs"));
        SignHandler signHandler = new SignHandler();
        for (RegistryKey<Block> key : Objects.requireNonNull(tag)) {
            blockManager.registerHandler(key.key(), () -> signHandler);
        }

        byte[] favicon;

        try (InputStream stream = Main.class.getResourceAsStream("/minestom.png")) {
            favicon = Objects.requireNonNull(stream).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            Status.PlayerInfo.Builder builder = Status.PlayerInfo.builder(Status.PlayerInfo.online(20))
                    .sample("The first line is separated from the others")
                    .sample("Could be a name, or a message");

            // on modern versions, you can obtain the player connection directly from the event
            if (event.getConnection() != null) {
                String ip = event.getConnection().getServerAddress();
                builder = builder
                        .sample("IP test: " + event.getConnection().getRemoteAddress().toString())
                        .sample("Connection Info:")
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" IP: ", NamedTextColor.GRAY))
                                .append(Component.text(ip != null ? ip : "???", NamedTextColor.YELLOW)))
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" PORT: ", NamedTextColor.GRAY))
                                .append(Component.text(event.getConnection().getServerPort())))
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" VERSION: ", NamedTextColor.GRAY))
                                .append(Component.text(event.getConnection().getProtocolVersion())));
            }

            builder = builder
                    .sample(Component.text("Time", NamedTextColor.YELLOW)
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(System.currentTimeMillis(), Style.style(TextDecoration.ITALIC))))
                    // components will be converted the legacy section sign format so they are displayed in the client
                    .sample(Component.text("You can use ").append(Component.text("styling too!", NamedTextColor.RED, TextDecoration.BOLD)));

            event.setStatus(Status.builder()
                    // the data will be automatically converted to the correct format on response, so you can do RGB and it'll be downsampled!
                    // on legacy versions, colors will be converted to the section format so it'll work there too
                    .description(Component.text("This is a Minestom Server", TextColor.color(0x66b3ff)))
                    .favicon(favicon)
                    .playerInfo(builder.build())
                    .build());
        });

        MinecraftServer.getRecipeManager().addRecipe(new ShapelessRecipe(
                RecipeBookCategory.CRAFTING_MISC,
                List.of(Material.DIRT),
                ItemStack.builder(Material.GOLD_BLOCK)
                        .set(DataComponents.CUSTOM_NAME, Component.text("abc"))
                        .build()
        ));

        new PlayerInit().init();

//        VelocityProxy.enable("abcdef");
        //BungeeCordProxy.enable();

//        MojangAuth.init();

        // useful for testing - we don't need to worry about event calls so just set this to a long time
        OpenToLAN.open(new OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)));

        minecraftServer.start("0.0.0.0", 25565);
//        minecraftServer.start(java.net.UnixDomainSocketAddress.of("minestom-demo.sock"));
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }
}
