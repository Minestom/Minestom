package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.world.Difficulty;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.Enum;

public record ServerDifficultyPacket(Difficulty difficulty, boolean locked) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ServerDifficultyPacket> SERIALIZER = NetworkBufferTemplate.template(
            Enum(Difficulty.class), ServerDifficultyPacket::difficulty,
            BOOLEAN, ServerDifficultyPacket::locked,
            ServerDifficultyPacket::new);
}
