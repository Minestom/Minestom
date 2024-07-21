package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record UpdateEnabledFeaturesPacket(@NotNull Set<NamespaceID> features) implements ServerPacket.Configuration {
    public static final int MAX_FEATURES = 1024;

    public UpdateEnabledFeaturesPacket(@NotNull NetworkBuffer buffer) {
        this(Set.copyOf(buffer.readCollection((b) -> NamespaceID.from(b.read(STRING)), MAX_FEATURES)));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(features, (b, feature) -> b.write(STRING, feature.asString()));
    }

}
