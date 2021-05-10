package net.minestom.server.permission;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Interface used to check if the {@link NBTCompound nbt data} of a {@link Permission} is correct.
 */
@FunctionalInterface
public interface PermissionVerifier {

    /**
     * Called when using {@link PermissionHandler#hasPermission(String, PermissionVerifier)}.
     *
     * @param nbtCompound the data of the permission, can be null if not any
     * @return true if {@link PermissionHandler#hasPermission(String, PermissionVerifier)}
     * should return true, false otherwise
     */
    boolean isValid(@Nullable NBTCompound nbtCompound);
}
