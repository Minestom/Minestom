package net.minestom.server.listener

import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket
import net.minestom.server.listener.EntityActionListener
import net.minestom.server.event.player.PlayerStartSneakingEvent
import net.minestom.server.event.player.PlayerStopSneakingEvent
import net.minestom.server.event.player.PlayerStartSprintingEvent
import net.minestom.server.event.player.PlayerStopSprintingEvent
import net.minestom.server.event.player.PlayerStartFlyingWithElytraEvent

object EntityActionListener {
    fun listener(packet: ClientEntityActionPacket, player: Player) {
        when (packet.action()) {
            ClientEntityActionPacket.Action.START_SNEAKING -> setSneaking(player, true)
            ClientEntityActionPacket.Action.STOP_SNEAKING -> setSneaking(player, false)
            ClientEntityActionPacket.Action.START_SPRINTING -> setSprinting(player, true)
            ClientEntityActionPacket.Action.STOP_SPRINTING -> setSprinting(player, false)
            ClientEntityActionPacket.Action.START_FLYING_ELYTRA -> startFlyingElytra(player)
        }
    }

    private fun setSneaking(player: Player, sneaking: Boolean) {
        val oldState = player.isSneaking
        player.isSneaking = sneaking
        if (oldState != sneaking) {
            if (sneaking) {
                EventDispatcher.call(PlayerStartSneakingEvent(player))
            } else {
                EventDispatcher.call(PlayerStopSneakingEvent(player))
            }
        }
    }

    private fun setSprinting(player: Player, sprinting: Boolean) {
        val oldState = player.isSprinting
        player.isSprinting = sprinting
        if (oldState != sprinting) {
            if (sprinting) {
                EventDispatcher.call(PlayerStartSprintingEvent(player))
            } else {
                EventDispatcher.call(PlayerStopSprintingEvent(player))
            }
        }
    }

    private fun startFlyingElytra(player: Player) {
        player.isFlyingWithElytra = true
        EventDispatcher.call(PlayerStartFlyingWithElytraEvent(player))
    }
}