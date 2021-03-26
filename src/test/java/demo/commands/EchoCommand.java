package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent;

public class EchoCommand extends Command {
    public EchoCommand() {
        super("echo");

        this.setDefaultExecutor((sender, context) -> sender.sendMessage(
                Component.text("Usage: /echo <json>")
                        .hoverEvent(Component.text("Click to get this command.")
                        .clickEvent(ClickEvent.suggestCommand("/echo ")))));

        ArgumentComponent json = ArgumentType.Component("json");

        this.addSyntax((sender, context) -> {
            sender.sendMessage(context.get(json));
        }, json);
    }
}
