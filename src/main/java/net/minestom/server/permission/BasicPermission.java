package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Basic {@link Permission} implementation that only requires the permission to be given to the {@link CommandSender} to be considered applied
 * (eg. no arguments)
 */
public class BasicPermission implements Permission<Object> {
    @Override
    public boolean isValidFor(@NotNull PermissionHandler permissionHandler, Object data) {
        return true;
    }
}
