package net.minestom.testing.miniclient

import io.netty.channel.ChannelHandlerContext
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import net.minestom.server.adventure.AdventureSerializer
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.PacketProcessor
import net.minestom.server.network.netty.packet.InboundPacket
import net.minestom.server.network.packet.handler.PacketsHandler
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket
import net.minestom.server.network.packet.server.login.LoginSuccessPacket
import net.minestom.server.network.packet.server.login.SetCompressionPacket
import net.minestom.server.network.packet.server.play.DisconnectPacket
import net.minestom.server.network.packet.server.play.JoinGamePacket
import net.minestom.server.network.player.PlayerConnection

class ClientSidePacketProcessor(val miniClient: MiniClient): PacketProcessor<ServerPacket, ServerPacket>() {

    override fun getPlayerConnection(context: ChannelHandlerContext?): PlayerConnection? {
        return miniClient.serverConnection
    }

    override fun createPlayPacketsHandler() = ServerPlayPacketsHandler()

    override fun createLoginPacketsHandler() = ServerLoginPacketsHandler()

    override fun createStatusPacketsHandler() = ServerStatusPacketsHandler()

    override fun processPlayPacket(playerConnection: PlayerConnection, playPacket: ServerPacket) {
        miniClient.receivePacket(playPacket)

        when(playPacket) {
            is JoinGamePacket -> {
                miniClient.playerInfo.entityID = playPacket.entityId
            }

            is DisconnectPacket -> {
                error("Disconnected: ${PlainComponentSerializer.plain().serialize(playPacket.message)}")
            }
        }
    }

    override fun processLoginPacket(playerConnection: PlayerConnection, statusPacket: ServerPacket) {
        when(statusPacket) {
            is LoginDisconnectPacket -> {
                error("Kicked, reason is: ${AdventureSerializer.serialize(statusPacket.kickMessage)}")
            }

            is LoginSuccessPacket -> {
                miniClient.playerInfo.uuid = statusPacket.uuid
                miniClient.playerInfo.username = statusPacket.username

                miniClient.serverConnection.connectionState = ConnectionState.PLAY
            }

            is SetCompressionPacket -> {
                miniClient.compressionThreshold = statusPacket.threshold

                miniClient.startCompression()
            }

            else -> TODO(statusPacket.javaClass.canonicalName)
        }
    }

    override fun processStatusPacket(playerConnection: PlayerConnection?, statusPacket: ServerPacket?) {
        TODO("Not yet implemented (status)")
    }

}
