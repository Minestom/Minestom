package net.minestom.testing.miniclient

import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.player.PlayerConnection

class ServerConnection(val miniClient: MiniClient): PlayerConnection() {

    override fun getRemoteAddress() = miniClient.remoteAddress

    override fun sendPacket(serverPacket: ServerPacket, skipTranslating: Boolean) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

}
