package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.featureflag.FeatureFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record UpdateEnabledFeaturesPacket(@NotNull Set<FeatureFlag> features) implements ServerPacket.Configuration {
    public static final int MAX_FEATURES = 64;

    public UpdateEnabledFeaturesPacket(@NotNull NetworkBuffer buffer) {
        this(Set.copyOf(buffer.readCollection((b) -> FeatureFlag.fromNamespaceId(b.read(STRING)), MAX_FEATURES)));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(features, (b, feature) -> b.write(STRING, feature.namespace().toString()));
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_UPDATE_ENABLED_FEATURES;
    }
}
