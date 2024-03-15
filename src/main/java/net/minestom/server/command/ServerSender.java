package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.OpPermissionHandler;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Sender used in {@link CommandManager#executeServerCommand(String)}.
 * <p>
 * Although this class implemented {@link CommandSender} and thus {@link Audience}, no
 * data can be sent to this sender because it's purpose is to process the data of
 * {@link CommandContext#getReturnData()}.
 */
public class ServerSender implements CommandSender {

    private final PermissionHandler permissionHandler = new OpPermissionHandler();
    private final TagHandler tagHandler = TagHandler.newHandler();

    @NotNull
    @Override
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    @Override
    public @NotNull Identity identity() {
        return Identity.nil();
    }
}