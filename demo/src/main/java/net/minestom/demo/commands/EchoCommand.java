package net.minestom.demo.commands;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;

public class EchoCommand extends Command {
    public EchoCommand() {
        super("echo");

        this.setDefaultExecutor((sender, context) -> sender.sendMessage(
                Component.text("Usage: /echo <json> [uuid]")
                        .hoverEvent(Component.text("Click to get this command.")
                        .clickEvent(ClickEvent.suggestCommand("/echo ")))));

        ArgumentComponent json = ArgumentType.Component("json");
        ArgumentUUID uuid = ArgumentType.UUID("uuid");

        this.addSyntax((sender, context) -> {
            sender.sendMessage(context.get(json));
        }, json);

        this.addSyntax((sender, context) -> {
            sender.sendMessage(Identity.identity(context.get(uuid)), context.get(json), MessageType.CHAT);
        }, uuid, json);
    }
}
