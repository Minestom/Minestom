package net.minestom.testing.miniclient

import net.minestom.server.network.packet.server.ServerPacketIdentifier
import net.minestom.server.network.packet.server.handler.ServerPacketsHandler
import net.minestom.server.network.packet.server.login.*
import java.util.*

class ServerLoginPacketsHandler: ServerPacketsHandler() {
    init {
        register(ServerPacketIdentifier.LOGIN_SUCCESS, ::LoginSuccessPacket)
        register(ServerPacketIdentifier.LOGIN_DISCONNECT, ::LoginDisconnectPacket)
        register(ServerPacketIdentifier.LOGIN_PLUGIN_REQUEST, ::LoginPluginRequestPacket)
        register(ServerPacketIdentifier.LOGIN_ENCRYPTION_REQUEST, ::EncryptionRequestPacket)
        register(ServerPacketIdentifier.LOGIN_SET_COMPRESSION, ::SetCompressionPacket)
    }
}