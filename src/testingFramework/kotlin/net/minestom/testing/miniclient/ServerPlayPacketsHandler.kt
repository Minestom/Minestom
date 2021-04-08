package net.minestom.testing.miniclient

import net.minestom.server.network.packet.server.ServerPacketIdentifier.*
import net.minestom.server.network.packet.server.handler.ServerPacketsHandler
import net.minestom.server.network.packet.server.play.*

class ServerPlayPacketsHandler: ServerPacketsHandler() {
    init {
        register(DISCONNECT, ::DisconnectPacket)
        register(CHUNK_DATA, ::ChunkDataPacket)
        register(UPDATE_LIGHT, ::UpdateLightPacket)
        register(PLAYER_POSITION_AND_LOOK, ::PlayerPositionAndLookPacket)
        register(ENTITY_TELEPORT, ::EntityTeleportPacket)
        register(JOIN_GAME, ::JoinGamePacket)
    }
}