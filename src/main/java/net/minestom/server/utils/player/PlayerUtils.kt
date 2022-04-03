package net.minestom.server.utils.player

import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.network.player.PlayerSocketConnection
import net.minestom.server.utils.player.PlayerUtils

object PlayerUtils {
    fun isSocketClient(playerConnection: PlayerConnection?): Boolean {
        return playerConnection is PlayerSocketConnection
    }

    fun isSocketClient(player: Player): Boolean {
        return isSocketClient(player.playerConnection)
    }

    @JvmStatic
    fun isSocketClient(entity: Entity?): Boolean {
        return entity is Player && isSocketClient(entity)
    }
}