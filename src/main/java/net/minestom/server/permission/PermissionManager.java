package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;

public interface PermissionManager {

    boolean verify(CommandSender sender, String permission);

    void registerPermission(Permission permission);

}
