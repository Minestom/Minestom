package net.minestom.testing.miniclient

import net.minestom.server.network.packet.server.handler.ServerPacketsHandler
import net.minestom.server.network.packet.server.play.ChunkDataPacket
import net.minestom.server.network.packet.server.play.DisconnectPacket
import net.minestom.server.network.packet.server.play.UpdateLightPacket

class ServerPlayPacketsHandler: ServerPacketsHandler() {
    init {
        register(0x19, ::DisconnectPacket)
        register(0x20, ::ChunkDataPacket)
        register(0x23, ::UpdateLightPacket)
    }
}