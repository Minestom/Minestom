package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientLoginPluginResponsePacket(int messageId, byte @Nullable [] data) implements ClientPacket {

    public ClientLoginPluginResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readOptional(RAW_BYTES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, messageId);
        writer.writeOptional(RAW_BYTES, data);
    }

}
