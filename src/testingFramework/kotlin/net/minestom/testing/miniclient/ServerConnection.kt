package net.minestom.testing.miniclient

import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.player.PlayerConnection
import java.net.SocketAddress

class ServerConnection: PlayerConnection() {
    override fun sendPacket(serverPacket: ServerPacket, skipTranslating: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getRemoteAddress(): SocketAddress {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

}
