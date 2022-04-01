package net.minestom.demo

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.block.BlockManager
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule
import net.minestom.server.command.CommandManager
import net.minestom.server.utils.callback.CommandCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import java.lang.Runnable
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.utils.identity.NamedAndIdentified
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextColor
import net.minestom.demo.PlayerInit
import net.minestom.demo.commands.*
import net.minestom.server.command.CommandSender
import net.minestom.server.extras.optifine.OptifineSupport
import net.minestom.server.extras.lan.OpenToLAN
import net.minestom.server.extras.lan.OpenToLANConfig
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val minecraftServer = MinecraftServer.init()
        val blockManager = MinecraftServer.getBlockManager()
        blockManager.registerBlockPlacementRule(RedstonePlacementRule())
        val commandManager = MinecraftServer.getCommandManager()
        commandManager.register(TestCommand())
        commandManager.register(EntitySelectorCommand())
        commandManager.register(HealthCommand())
        commandManager.register(LegacyCommand())
        commandManager.register(DimensionCommand())
        commandManager.register(ShutdownCommand())
        commandManager.register(TeleportCommand())
        commandManager.register(PlayersCommand())
        commandManager.register(FindCommand())
        commandManager.register(PotionCommand())
        commandManager.register(TitleCommand())
        commandManager.register(BookCommand())
        commandManager.register(ShootCommand())
        commandManager.register(HorseCommand())
        commandManager.register(EchoCommand())
        commandManager.register(SummonCommand())
        commandManager.register(RemoveCommand())
        commandManager.register(GiveCommand())
        commandManager.register(SetBlockCommand())
        commandManager.register(AutoViewCommand())
        commandManager.register(SaveCommand())
        commandManager.register(GamemodeCommand())
        commandManager.unknownCommandCallback = CommandCallback { sender: CommandSender, command: String? ->
            sender.sendMessage(
                Component.text("Unknown command", NamedTextColor.RED)
            )
        }
        MinecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND))
        MinecraftServer.getSchedulerManager().buildShutdownTask { println("Good night") }
        MinecraftServer.getGlobalEventHandler()
            .addListener(ServerListPingEvent::class.java) { event: ServerListPingEvent ->
                val responseData = event.responseData
                responseData.addEntry(NamedAndIdentified.named("The first line is separated from the others"))
                responseData.addEntry(NamedAndIdentified.named("Could be a name, or a message"))

                // on modern versions, you can obtain the player connection directly from the event
                if (event.connection != null) {
                    responseData.addEntry(NamedAndIdentified.named("IP test: " + event.connection!!.remoteAddress.toString()))
                    responseData.addEntry(NamedAndIdentified.named("Connection Info:"))
                    val ip = event.connection!!.serverAddress
                    responseData.addEntry(
                        NamedAndIdentified.named(
                            Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" IP: ", NamedTextColor.GRAY))
                                .append(Component.text(ip ?: "???", NamedTextColor.YELLOW))
                        )
                    )
                    responseData.addEntry(
                        NamedAndIdentified.named(
                            Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" PORT: ", NamedTextColor.GRAY))
                                .append(Component.text(event.connection!!.serverPort))
                        )
                    )
                    responseData.addEntry(
                        NamedAndIdentified.named(
                            Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" VERSION: ", NamedTextColor.GRAY))
                                .append(Component.text(event.connection!!.protocolVersion))
                        )
                    )
                }
                responseData.addEntry(
                    NamedAndIdentified.named(
                        Component.text("Time", NamedTextColor.YELLOW)
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(System.currentTimeMillis(), Style.style(TextDecoration.ITALIC)))
                    )
                )

                // components will be converted the legacy section sign format so they are displayed in the client
                responseData.addEntry(
                    NamedAndIdentified.named(
                        Component.text("You can use ")
                            .append(Component.text("styling too!", NamedTextColor.RED, TextDecoration.BOLD))
                    )
                )

                // the data will be automatically converted to the correct format on response, so you can do RGB and it'll be downsampled!
                // on legacy versions, colors will be converted to the section format so it'll work there too
                responseData.description = Component.text("This is a Minestom Server", TextColor.color(0x66b3ff))
            }
        PlayerInit.init()
        OptifineSupport.enable()

        //VelocityProxy.enable("rBeJJ79W4MVU");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        // useful for testing - we don't need to worry about event calls so just set this to a long time
        OpenToLAN.open(OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)))
        minecraftServer.start("0.0.0.0", 25565)
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }
}