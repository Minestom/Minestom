package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static net.minestom.server.network.NetworkBuffer.*;

public record UpdateEnabledFeaturesPacket(Collection<NamespaceID> featureFlags) implements ServerPacket {
    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, featureFlags.size());
        for (var featureFlag : featureFlags) {
            writer.write(STRING, featureFlag.asString());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_ENABLED_FEATURES;
    }
}
