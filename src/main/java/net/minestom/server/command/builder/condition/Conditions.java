package net.minestom.server.command.builder.condition;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common command conditions to be used as a method reference, e.g. {@code Conditions::playerOnly}.
 */
public class Conditions {

    /**
     * @return true if the sender is a player. If they are not, they are sent a translatable error message
     */
    public static boolean playerOnly(@NotNull CommandSender sender, @Nullable String commandString) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    /**
     * @return true if the sender is the console. If they are not, they are sent the text "The console is required to
     * run this command here".
     */
    public static boolean consoleOnly(@NotNull CommandSender sender, @Nullable String commandString) {
        if (!(sender instanceof ConsoleSender)) {
            sender.sendMessage(Component.text("The console is required to run this command here", NamedTextColor.RED));
            return false;
        }
        return true;
    }
}
