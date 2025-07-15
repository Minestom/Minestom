package net.minestom.server.network.player;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record GameProfile(UUID uuid, String name,
                          List<Property> properties) {
    public static final int MAX_PROPERTIES = 1024;

    public GameProfile {
        if (name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        if (name.length() > 16)
            throw new IllegalArgumentException("Name length cannot be greater than 16 characters");
        properties = List.copyOf(properties);
    }

    public GameProfile(UUID uuid, String name) {
        this(uuid, name, List.of());
    }

    public static final NetworkBuffer.Type<GameProfile> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID, GameProfile::uuid,
            STRING, GameProfile::name,
            Property.SERIALIZER.list(MAX_PROPERTIES), GameProfile::properties,
            GameProfile::new
    );

    public record Property(String name, String value, @Nullable String signature) {
        public Property(String name, String value) {
            this(name, value, null);
        }

        public static final NetworkBuffer.Type<Property> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Property::name,
                STRING, Property::value,
                STRING.optional(), Property::signature,
                Property::new
        );
    }
}
