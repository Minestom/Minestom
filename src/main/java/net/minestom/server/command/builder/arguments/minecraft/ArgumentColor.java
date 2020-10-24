package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you a {@link ChatColor}.
 * <p>
 * Example: red, white, reset
 */
public class ArgumentColor extends Argument<ChatColor> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentColor(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        final ChatColor color = ChatColor.fromName(value);
        return color == ChatColor.NO_COLOR ? UNDEFINED_COLOR : SUCCESS;
    }

    @NotNull
    @Override
    public ChatColor parse(@NotNull String value) {
        return ChatColor.fromName(value);
    }

    @Override
    public int getConditionResult(@NotNull ChatColor value) {
        return SUCCESS;
    }
}
