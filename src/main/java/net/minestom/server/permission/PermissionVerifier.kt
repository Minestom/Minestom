package net.minestom.server.permission

import org.jglrxavpok.hephaistos.nbt.NBTCompound
import net.minestom.server.permission.PermissionVerifier

/**
 * Interface used to check if the [nbt data][NBTCompound] of a [Permission] is correct.
 */
fun interface PermissionVerifier {
    /**
     * Called when using [PermissionHandler.hasPermission].
     *
     * @param nbtCompound the data of the permission, can be null if not any
     * @return true if [PermissionHandler.hasPermission]
     * should return true, false otherwise
     */
    fun isValid(nbtCompound: NBTCompound?): Boolean
}