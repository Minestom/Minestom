package net.minestom.server.network.player;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record GameProfile(@NotNull UUID uuid, @NotNull String name,
                          @NotNull List<@NotNull Property> properties) {
    public static final int MAX_PROPERTIES = 1024;

    public GameProfile {
        if (name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        if (name.length() > 16)
            throw new IllegalArgumentException("Name length cannot be greater than 16 characters");
        properties = List.copyOf(properties);
    }

    public static final NetworkBuffer.Type<GameProfile> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID, GameProfile::uuid,
            STRING, GameProfile::name,
            Property.SERIALIZER.list(MAX_PROPERTIES), GameProfile::properties,
            GameProfile::new
    );

    public record Property(@NotNull String name, @NotNull String value, @Nullable String signature) {
        public Property(@NotNull String name, @NotNull String value) {
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
