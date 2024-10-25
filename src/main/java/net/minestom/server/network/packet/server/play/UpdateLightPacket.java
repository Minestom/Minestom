package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateLightPacket(int chunkX, int chunkZ,
                                @NotNull LightData lightData) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateLightPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, UpdateLightPacket::chunkX,
            VAR_INT, UpdateLightPacket::chunkZ,
            LightData.SERIALIZER, UpdateLightPacket::lightData,
            UpdateLightPacket::new
    );
}
