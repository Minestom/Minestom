package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record UpdateEnabledFeaturesPacket(Set<NamespaceID> featureFlags) implements ServerPacket {

    public UpdateEnabledFeaturesPacket {
        featureFlags = Set.copyOf(featureFlags);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(featureFlags, ((w, value) -> w.write(STRING, value.asString())));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_ENABLED_FEATURES;
    }
}
