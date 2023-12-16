package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record UpdateEnabledFeaturesPacket(@NotNull Set<NamespaceID> features) implements ServerPacket {

    public UpdateEnabledFeaturesPacket(@NotNull NetworkBuffer buffer) {
        this(Set.copyOf(buffer.readCollection((b) -> NamespaceID.from(b.read(STRING)))));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(features, (b, feature) -> b.write(STRING, feature.asString()));
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_UPDATE_ENABLED_FEATURES;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION);
        };
    }

}
