package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record UpdateEnabledFeaturesPacket(@NotNull List<String> features) implements ServerPacket.Configuration {
    public static final int MAX_FEATURES = 1024;

    public UpdateEnabledFeaturesPacket {
        if (features.size() > MAX_FEATURES)
            throw new IllegalArgumentException("Too many features");
        features = List.copyOf(features);
    }

    public static NetworkBuffer.Type<UpdateEnabledFeaturesPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING.list(MAX_FEATURES), UpdateEnabledFeaturesPacket::features,
            UpdateEnabledFeaturesPacket::new
    );
}
