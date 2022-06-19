package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatPreviewEvent;

import java.util.function.Consumer;

public class ChatPreviewCommand extends Command {
    private static final ArgumentBoolean on = ArgumentType.Boolean("on");
    private static final ArgumentEnum<PreviewHandler> handlerArg = ArgumentType.Enum("handler", PreviewHandler.class);
    private static PreviewHandler handler = PreviewHandler.NULL;

    private enum PreviewHandler {
        BLACK(e -> e.setResult(Component.text(e.getQuery(), NamedTextColor.BLACK))),
        //TODO Better name?
        ENCLOSE(e -> e.setResult(Component.empty()
                .append(Component.text("PREPEND>", TextColor.color(255,0,255)))
                .append(Component.text(e.getQuery()))
                .append(Component.text("<APPEND", TextColor.color(255,0,255))))),
        NULL(e -> e.setResult(null));

        private final Consumer<PlayerChatPreviewEvent> eventConsumer;

        PreviewHandler(Consumer<PlayerChatPreviewEvent> eventConsumer) {
            this.eventConsumer = eventConsumer;
        }
    }

    public ChatPreviewCommand() {
        super("preview");

        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatPreviewEvent.class, e -> handler.eventConsumer.accept(e));

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                final Boolean enable = context.get(on);
                player.enableChatPreview(enable);
                player.sendMessage("Chat preview: " + (enable ? "enabled" : "disabled"));
            }
        }), ArgumentType.Literal("enable"), on);

        addSyntax(((sender, context) -> {
            handler = context.getOrDefault(handlerArg, PreviewHandler.NULL);
            sender.sendMessage("Preview handler set to: " + handler.name());
        }), ArgumentType.Literal("handler"), handlerArg);


        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                final Boolean enable = context.get(on);
                player.toggleChatPreview(enable);
                player.sendMessage("Chat preview: " + (enable ? "on" : "off"));
            }
        }), ArgumentType.Literal("toggle"), on);
    }
}
