package demo.commands;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;
import java.util.Locale;

/**
 * Command that make a player change gamemode, made in the style of the vanilla /gamemode command.
 * <br>
 * See https://minecraft.fandom.com/wiki/Commands/gamemode
 */
public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        //GameMode parameter
        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        gamemode.setCallback((origin, exception) -> {
            origin.sender().sendMessage(Component.text("Invalid gamemode", NamedTextColor.RED));
            origin.sender().sendMessage(exception.generateContextMessage());
        });

        ArgumentEntity player = ArgumentType.Entity("targets").onlyPlayers(true);

        // Upon invalid usage, print the correct usage of the command to the sender
        setDefaultExecutor((origin, context) -> {
            String commandName = context.getCommand().getName();

            origin.sender().sendMessage(Component.text("Usage: /" + commandName + " <gamemode> [targets]", NamedTextColor.RED), MessageType.SYSTEM);
        });

        //Command Syntax for /gamemode <gamemode>
        addSyntax((origin, context) -> {
            //Limit execution to players only
            if (!(origin.entity() instanceof Player playerOrigin)) {
                origin.sender().sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
                return;
            }

            //Check permission, this could be replaced with hasPermission
            if (playerOrigin.getPermissionLevel() < 2) {
                origin.sender().sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                return;
            }

            GameMode mode = context.get(gamemode);

            //Set the gamemode for the sender
            executeSelf(origin, mode);
        }, gamemode);

        //Command Syntax for /gamemode <gamemode> [targets]
        addSyntax((origin, context) -> {
            //Check permission for players only
            //This allows the console to use this syntax too
            if (origin.entity() instanceof Player p && p.getPermissionLevel() < 2) {
                origin.sender().sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
                return;
            }

            EntityFinder finder = context.get(player);
            GameMode mode = context.get(gamemode);

            //Set the gamemode for the targets
            executeOthers(origin, mode, finder.find(origin.sender()));
        }, gamemode, player);
    }

    /**
     * Sets the gamemode for the specified entities, and
     * notifies them (and the sender) in the chat.
     */
    private void executeOthers(CommandOrigin origin, GameMode mode, List<Entity> entities) {
        if (entities.size() == 0) {
            //If there are no players that could be modified, display an error message
            origin.sender().sendMessage(Component.translatable("argument.entity.notfound.player", NamedTextColor.RED), MessageType.SYSTEM);
        } else for (Entity entity : entities) {
            if (entity instanceof Player p) {
                if (p == origin.entity()) {
                    //If the player is the same as the sender, call
                    //executeSelf to display one message instead of two
                    executeSelf(origin, mode);
                } else {
                    p.setGameMode(mode);

                    String gamemodeString = "gameMode." + mode.name().toLowerCase(Locale.ROOT);
                    Component gamemodeComponent = Component.translatable(gamemodeString);
                    Component playerName = p.getDisplayName() == null ? p.getName() : p.getDisplayName();

                    //Send a message to the changed player and the sender
                    p.sendMessage(Component.translatable("gameMode.changed", gamemodeComponent), MessageType.SYSTEM);
                    origin.sender().sendMessage(Component.translatable("commands.gamemode.success.other", playerName, gamemodeComponent), MessageType.SYSTEM);
                }
            }
        }
    }

    /**
     * Sets the gamemode for the executing Player, and
     * notifies them in the chat.
     */
    private void executeSelf(CommandOrigin origin, GameMode mode) {
        if (!(origin.entity() instanceof Player player)) {
            return;
        }
        player.setGameMode(mode);

        //The translation keys 'gameMode.survival', 'gameMode.creative', etc.
        //correspond to the translated game mode names.
        String gamemodeString = "gameMode." + mode.name().toLowerCase(Locale.ROOT);
        Component gamemodeComponent = Component.translatable(gamemodeString);

        //Send the translated message to the player.
        origin.sender().sendMessage(Component.translatable("commands.gamemode.success.self", gamemodeComponent), MessageType.SYSTEM);
    }
}
