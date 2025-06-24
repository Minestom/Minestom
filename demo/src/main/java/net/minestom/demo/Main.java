package net.minestom.demo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.demo.block.TestBlockHandler;
import net.minestom.demo.block.placement.BedPlacementRule;
import net.minestom.demo.block.placement.DripstonePlacementRule;
import net.minestom.demo.commands.*;
import net.minestom.demo.recipe.ShapelessRecipe;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.component.DataComponents;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.dialog.DialogAction;
import net.minestom.server.dialog.DialogActionButton;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.dialog.DialogBody;
import net.minestom.server.dialog.DialogInput;
import net.minestom.server.dialog.DialogMetadata;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.lan.OpenToLANConfig;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        MinecraftServer.setCompressionThreshold(0);

        RegistryKey<Dialog> newInput = RegistryKey.unsafeOf("cookie:test");
        MinecraftServer.detourRegistryInit().register(BuiltinRegistries.DIALOG, (dialogRegistry) -> {
            var dialog = new Dialog.MultiAction(
                    new DialogMetadata(
                            Component.text("Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))),
                            null, true, false,
                            DialogAfterAction.CLOSE,
                            List.of(
                                    new DialogBody.PlainMessage(Component.text("plain message here").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))), DialogBody.PlainMessage.DEFAULT_WIDTH),
                                    new DialogBody.Item(ItemStack.of(Material.DIAMOND, 5),
                                            new DialogBody.PlainMessage(Component.text("item message"), DialogBody.PlainMessage.DEFAULT_WIDTH),
                                            false, true, 16, 16)
                            ),
                            List.of(
                                    new DialogInput.Text("text", DialogInput.DEFAULT_WIDTH * 2, Component.text("Enter some text")
                                            .hoverEvent(HoverEvent.showText(Component.text("Hover text here"))), true, "", Integer.MAX_VALUE, new DialogInput.Text.Multiline(15, null)),
                                    new DialogInput.Boolean("bool", Component.text("Checkbox"), false, "true", "false"),
                                    new DialogInput.SingleOption("single_option", DialogInput.DEFAULT_WIDTH, List.of(
                                            new DialogInput.SingleOption.Option("option1", Component.text("Option 1"), true),
                                            new DialogInput.SingleOption.Option("option2", Component.text("Option 2"), false),
                                            new DialogInput.SingleOption.Option("option3", Component.text("Option 3"), false)
                                    ), Component.text("Single option"), true),
                                    new DialogInput.NumberRange("number_range", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r2ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r3ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r4ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r5ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r6ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f)
                            )
                    ),
                    List.of(
                            new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, new DialogAction.DynamicCustom(Key.key("done_action"), null)),
                            new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, null)
                    ),
                    null, 2
            );
            dialogRegistry.register(newInput, dialog);
            return dialogRegistry;
        });

        MinecraftServer minecraftServer = MinecraftServer.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new DripstonePlacementRule());
        var beds = Block.values().stream().filter(block -> block.registry().blockEntityId() == 25).toList();
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
        commandManager.register(new TestInstabreakCommand());
        commandManager.register(new AttributeCommand());
        commandManager.register(new PrimedTNTCommand());
        commandManager.register(new SleepCommand());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));

        MinecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND));

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night"));

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            ResponseData responseData = event.getResponseData();
            responseData.addEntry(NamedAndIdentified.named("The first line is separated from the others"));
            responseData.addEntry(NamedAndIdentified.named("Could be a name, or a message"));

            // on modern versions, you can obtain the player connection directly from the event
            if (event.getConnection() != null) {
                responseData.addEntry(NamedAndIdentified.named("IP test: " + event.getConnection().getRemoteAddress().toString()));

                responseData.addEntry(NamedAndIdentified.named("Connection Info:"));
                String ip = event.getConnection().getServerAddress();
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" IP: ", NamedTextColor.GRAY))
                        .append(Component.text(ip != null ? ip : "???", NamedTextColor.YELLOW))));
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" PORT: ", NamedTextColor.GRAY))
                        .append(Component.text(event.getConnection().getServerPort()))));
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" VERSION: ", NamedTextColor.GRAY))
                        .append(Component.text(event.getConnection().getProtocolVersion()))));
            }
            responseData.addEntry(NamedAndIdentified.named(Component.text("Time", NamedTextColor.YELLOW)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.text(System.currentTimeMillis(), Style.style(TextDecoration.ITALIC)))));

            // components will be converted the legacy section sign format so they are displayed in the client
            responseData.addEntry(NamedAndIdentified.named(Component.text("You can use ").append(Component.text("styling too!", NamedTextColor.RED, TextDecoration.BOLD))));

            // the data will be automatically converted to the correct format on response, so you can do RGB and it'll be downsampled!
            // on legacy versions, colors will be converted to the section format so it'll work there too
            responseData.setDescription(Component.text("This is a Minestom Server", TextColor.color(0x66b3ff)));
            //responseData.setPlayersHidden(true);
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
