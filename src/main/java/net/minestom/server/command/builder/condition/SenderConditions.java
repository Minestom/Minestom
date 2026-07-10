package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Common command conditions branching on the concrete {@link CommandSender} type.
 * <p>
 * Structural conditions ({@code all}/{@code any}/{@code not}) live on {@link Conditions}.
 */
public final class SenderConditions {
    private SenderConditions() {
    }

    /**
     * Will succeed if the command sender is a player.
     */
    public static boolean playerOnly(CommandSender sender, @Nullable String commandString) {
        return sender instanceof Player;
    }

    /**
     * Will succeed if the command sender is the server console.
     */
    public static boolean consoleOnly(CommandSender sender, @Nullable String commandString) {
        return sender instanceof ConsoleSender;
    }
}
