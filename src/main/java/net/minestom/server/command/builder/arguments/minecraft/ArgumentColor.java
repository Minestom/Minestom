package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.builder.arguments.Argument;

/**
 * Represent an argument which will give you a {@link ChatColor}
 * Chat format: red, white, reset, etc...
 */
public class ArgumentColor extends Argument<ChatColor> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentColor(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(String value) {
        ChatColor color = ChatColor.fromName(value);
        return color == ChatColor.NO_COLOR ? UNDEFINED_COLOR : SUCCESS;
    }

    @Override
    public ChatColor parse(String value) {
        return ChatColor.fromName(value);
    }

    @Override
    public int getConditionResult(ChatColor value) {
        return SUCCESS;
    }
}
