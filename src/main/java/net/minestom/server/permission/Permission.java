package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;

/**
 * Representation of a permission granted to a {@link CommandSender}.
 * Each permission has a string representation used as an identifier, and an optional
 * {@link NBTCompound} used to store additional data.
 * <p>
 * The class is immutable.
 */
public class Permission {

    private final String permissionName;
    private final NBTCompound data;

    /**
     * Creates a new permission object with optional data.
     *
     * @param permissionName the name of the permission
     * @param data           the optional data of the permission
     */
    public Permission(@NotNull String permissionName, @Nullable NBTCompound data) {
        this.permissionName = permissionName;
        this.data = data;
    }

    /**
     * Creates a new permission object without additional data
     *
     * @param permissionName the name of the permission
     */
    public Permission(@NotNull String permissionName) {
        this(permissionName, null);
    }

    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    @NotNull
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * Gets the data associated to this permission.
     *
     * @return the nbt data of this permission, can be null if not any
     */
    @Nullable
    public NBTCompound getNBTData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionName.equals(that.permissionName) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionName, data);
    }
}
