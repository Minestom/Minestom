package net.minestom.testing.miniclient

import io.netty.channel.ChannelHandlerContext
import net.minestom.server.network.PacketProcessor
import net.minestom.server.network.netty.packet.InboundPacket
import net.minestom.server.network.packet.handler.PacketsHandler
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.player.PlayerConnection

class ClientSidePacketProcessor: PacketProcessor<ServerPacket, ServerPacket>() {

    private val serverConnection = ServerConnection()

    override fun process(context: ChannelHandlerContext, packet: InboundPacket) {
        //TODO("Not yet implemented")
//        NotImplementedError("An operation is not implemented").printStackTrace()
    }

    override fun getPlayerConnection(context: ChannelHandlerContext?): PlayerConnection? {
        return serverConnection
    }

    override fun createPlayPacketsHandler() = ServerPlayPacketsHandler()

    override fun createLoginPacketsHandler() = ServerLoginPacketsHandler()

    override fun createStatusPacketsHandler() = ServerStatusPacketsHandler()

    override fun processPlayPacket(playerConnection: PlayerConnection?, playPacket: ServerPacket?) {
        TODO("Not yet implemented (play)")
    }

    override fun processLoginPacket(playerConnection: PlayerConnection?, statusPacket: ServerPacket?) {
        TODO("Not yet implemented (login)")
    }

    override fun processStatusPacket(playerConnection: PlayerConnection?, statusPacket: ServerPacket?) {
        TODO("Not yet implemented (status)")
    }

}
