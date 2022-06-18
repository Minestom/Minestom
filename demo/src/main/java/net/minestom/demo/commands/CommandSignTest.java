package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.ArgumentsSignature;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentMessage;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.crypto.SignatureValidator;
import net.minestom.server.entity.Player;
import net.minestom.server.message.Messenger;

import java.util.List;

public class CommandSignTest extends Command {
    private static final ArgumentMessage message = ArgumentType.Message("message");

    public CommandSignTest() {
        super("sign");

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                final ArgumentsSignature argumentsSignature = context.getSignature();
                if (argumentsSignature == null) {
                    Messenger.sendSystemMessage(List.of(player), Component.text("You didn't sign the arguments!", NamedTextColor.RED));
                    return;
                }
                final MessageSignature signature = argumentsSignature.signatureOf(message, player.getUuid());
                final SignatureValidator validator = SignatureValidator.from(player);
                if (validator == null) {
                    Messenger.sendSystemMessage(List.of(player), Component.text("There is no public key associated with your profile!", NamedTextColor.RED));
                    return;
                }
                Messenger.sendSystemMessage(List.of(player),
                        Component.text("Signature details: preview: ")
                                .append(formatBoolean(argumentsSignature.signedPreview()))
                                .append(Component.text(", argument_signature: "))
                                .append(format(SignatureValidator.validate(validator, signature, Component.text(context.get(message)))))
                                .append(Component.text(", preview_signature: "))
                                .append(format(SignatureValidator.validate(validator, signature, player.getLastPreviewedMessage()))));
            } else {
                sender.sendMessage("Consoles luckily doesn't sign commands...");
            }
        }), message);
    }

    private static Component format(boolean valid) {
        return valid ? Component.text("valid", NamedTextColor.GREEN) : Component.text("invalid", NamedTextColor.RED);
    }

    private static Component formatBoolean(boolean value) {
        return value ? Component.text("true", NamedTextColor.GREEN) : Component.text("false", NamedTextColor.RED);
    }
}
