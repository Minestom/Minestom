package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateLightPacket(int chunkX, int chunkZ,
                                @NotNull LightData lightData) implements ServerPacket.Play {
    public UpdateLightPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), new LightData(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, chunkX);
        writer.write(VAR_INT, chunkZ);
        writer.write(lightData);
    }

}
