package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.bungee.messaging.BungeeMessage;
import net.minestom.server.extras.bungee.messaging.BungeeRequest;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxyCommand extends Command {
    public ProxyCommand() {
        super("proxy");
        setCondition(ProxyCommand::canUse);
        setDefaultExecutor((sender, context) -> sender.sendMessage("A supported proxy " +
                (canUse(sender, "proxy") ? "is" : "not") + " enabled."));
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            new BungeeRequest.UUID().send(player);
            sender.sendMessage("Sent UUID request to BungeeCord/Velocity proxy.");
        }, ArgumentType.Literal("uuid"));
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            new BungeeRequest.IP().send(player);
            sender.sendMessage("Sent IP request to BungeeCord/Velocity proxy.");
        }, ArgumentType.Literal("ip"));
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            new BungeeRequest.PlayerCount("minestom").send(player);
            sender.sendMessage("Sent player count request to BungeeCord/Velocity proxy.");
        }, ArgumentType.Literal("playercount"));
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            new BungeeRequest.GetServers().send(player);
            sender.sendMessage("Sent serverList request to BungeeCord/Velocity proxy.");
        }, ArgumentType.Literal("serverlist"));
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            new BungeeRequest.PlayerList("minestom").send(player);
            sender.sendMessage("Sent player list request to BungeeCord/Velocity proxy.");
        }, ArgumentType.Literal("playerlist"));
    }

    private static boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return Conditions.playerOnly(sender, commandString) &&
                (VelocityProxy.isEnabled() || BungeeCordProxy.isEnabled());
    }
}
