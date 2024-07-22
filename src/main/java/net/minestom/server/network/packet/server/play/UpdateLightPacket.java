package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateLightPacket(int chunkX, int chunkZ,
                                @NotNull LightData lightData) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateLightPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, UpdateLightPacket value) {
            buffer.write(VAR_INT, value.chunkX);
            buffer.write(VAR_INT, value.chunkZ);
            buffer.write(value.lightData);
        }

        @Override
        public UpdateLightPacket read(@NotNull NetworkBuffer buffer) {
            return new UpdateLightPacket(buffer.read(VAR_INT), buffer.read(VAR_INT), new LightData(buffer));
        }
    };
}
