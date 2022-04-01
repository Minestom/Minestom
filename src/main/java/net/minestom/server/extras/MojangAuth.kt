package net.minestom.server.extras

import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.process
import net.minestom.server.ServerProcess.isAlive
import net.minestom.server.extras.mojangAuth.MojangCrypt.generateKeyPair
import net.minestom.server.MinecraftServer.Companion.blockManager
import net.minestom.server.extras.MojangAuth
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.mojangAuth.MojangCrypt
import net.minestom.server.instance.block.BlockManager
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule
import java.security.KeyPair

object MojangAuth {
    @JvmField
    val AUTH_URL = System.getProperty(
        "minestom.auth.url",
        "https://sessionserver.mojang.com/session/minecraft/hasJoined"
    ) + "?username=%s&serverId=%s"

    @Volatile
    var isEnabled = false
        private set

    @JvmStatic
    @Volatile
    var keyPair: KeyPair? = null
        private set

    /**
     * Enables mojang authentication on the server.
     *
     *
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    fun init() {
        stateCondition(isEnabled, "Mojang auth is already enabled!")
        stateCondition(process()!!.isAlive, "The server has already been started!")
        isEnabled = true
        // Generate necessary fields...
        keyPair = generateKeyPair()
    }
}