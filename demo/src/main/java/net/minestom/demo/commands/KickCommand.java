package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick");
        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /kick <player> [reason]")));

        var playerArg = ArgumentType.Entity("player").onlyPlayers(true);
        var reasonArg = ArgumentType.StringArray("reason");

        reasonArg.setSuggestionCallback((sender, context, suggestion) -> {
            if (suggestion.getInput().isBlank()) {
                suggestion.addEntry(new SuggestionEntry("<reason>"));
            }
        });

        addSyntax((sender, context) -> {
            EntityFinder finder = context.get("player");
            Component reason = Component.text("You have been kicked from this server");
            finder.find(sender).stream().filter(e -> e instanceof Player).map(e -> (Player) e).forEach(p -> {
                p.kick(reason);
                sender.sendMessage(Component.text("Kicked " + p.getUsername(), NamedTextColor.GREEN));
            });
        }, playerArg);

        addSyntax((sender, context) -> {
            EntityFinder finder = context.get("player");
            Component reason = Component.text(String.join(" ", context.get(reasonArg)));
            finder.find(sender).stream().filter(e -> e instanceof Player).map(e -> (Player) e).forEach(p -> {
                p.kick(reason);
                sender.sendMessage(Component.join(
                        JoinConfiguration.separator(Component.space()),
                        Component.text("Kicked " + p.getUsername() + ":", NamedTextColor.GREEN),
                        reason
                ));
            });
        }, playerArg, reasonArg);

    }

}
