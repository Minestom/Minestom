package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.world.Difficulty;

import static net.minestom.server.network.NetworkBuffer.Enum;

public record ClientChangeDifficultyPacket(Difficulty difficulty) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientChangeDifficultyPacket> SERIALIZER = NetworkBufferTemplate.template(
            Enum(Difficulty.class), ClientChangeDifficultyPacket::difficulty,
            ClientChangeDifficultyPacket::new);
}
