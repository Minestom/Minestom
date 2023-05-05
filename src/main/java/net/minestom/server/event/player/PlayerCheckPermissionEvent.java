package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

public class PlayerCheckPermissionEvent implements PlayerEvent {

    private final Player player;
    private final Permission permission;
    private final String permissionName;
    private boolean hasPermission;

    public PlayerCheckPermissionEvent(Player player, Permission permission, String permissionName, boolean hasPermission) {
        this.player = player;
        this.permission = permission;
        this.permissionName = permissionName;
        this.hasPermission = hasPermission;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    public Permission getPermission() {
        return permission;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public boolean hasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
