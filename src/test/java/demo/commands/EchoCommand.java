package demo.commands;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.CommandException;

public class EchoCommand extends Command {
    public EchoCommand() {
        super("echo");

        this.setDefaultExecutor((origin, context) -> origin.sender().sendMessage(
                Component.text("Usage: /echo <json> [uuid]")
                        .hoverEvent(Component.text("Click to get this command.")
                        .clickEvent(ClickEvent.suggestCommand("/echo ")))));

        var json = ArgumentType.Component("json").setCallback(CommandException.STANDARD_CALLBACK);
        var uuid = ArgumentType.UUID("uuid").setCallback(CommandException.STANDARD_CALLBACK);

        this.addSyntax((origin, context) -> {
            origin.sender().sendMessage(context.get(json));
        }, json);

        this.addSyntax((origin, context) -> {
            origin.sender().sendMessage(Identity.identity(context.get(uuid)), context.get(json), MessageType.CHAT);
        }, uuid, json);
    }
}
