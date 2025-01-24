package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.Enum;

public record ClientChangeDifficultyPacket(@NotNull Difficulty difficulty, boolean locked) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChangeDifficultyPacket> SERIALIZER = NetworkBufferTemplate.template(
            Enum(Difficulty.class), ClientChangeDifficultyPacket::difficulty,
            BOOLEAN, ClientChangeDifficultyPacket::locked,
            ClientChangeDifficultyPacket::new);
}
