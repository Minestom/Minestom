package net.minestom.server.listener

import net.minestom.server.entity.Player
import net.minestom.server.network.packet.client.play.ClientSettingsPacket
import net.minestom.server.entity.Player.PlayerSettings
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerSettingsChangeEvent

object SettingsListener {
    fun listener(packet: ClientSettingsPacket, player: Player) {
        val settings = player.settings
        val viewDistance = Math.abs(packet.viewDistance().toInt()).toByte()
        settings.refresh(
            packet.locale(),
            viewDistance,
            packet.chatMessageType(),
            packet.chatColors(),
            packet.displayedSkinParts(),
            packet.mainHand(),
            packet.enableTextFiltering(),
            packet.allowsListing()
        )
        EventDispatcher.call(PlayerSettingsChangeEvent(player))
    }
}