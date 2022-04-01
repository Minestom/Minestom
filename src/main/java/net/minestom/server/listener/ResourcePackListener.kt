package net.minestom.server.listener

import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.network.packet.client.play.ClientResourcePackStatusPacket
import net.minestom.server.event.player.PlayerResourcePackStatusEvent

object ResourcePackListener {
    @JvmStatic
    fun listener(packet: ClientResourcePackStatusPacket, player: Player?) {
        EventDispatcher.call(PlayerResourcePackStatusEvent(player!!, packet.status()))
    }
}