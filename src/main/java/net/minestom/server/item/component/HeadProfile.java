package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.UUID;

public record HeadProfile(@Nullable String name, @Nullable UUID uuid, List<Property> properties) {
    public static final HeadProfile EMPTY = new HeadProfile(null, null, List.of());

    public static final NetworkBuffer.Type<HeadProfile> NETWORK_TYPE = NetworkBufferTemplate.template(
            STRING.optional(), HeadProfile::name,
            UUID.optional(), HeadProfile::uuid,
            Property.NETWORK_TYPE.list(Short.MAX_VALUE), HeadProfile::properties,
            HeadProfile::new);
    public static final Codec<HeadProfile> CODEC = StructCodec.struct(
            "name", Codec.STRING.optional(), HeadProfile::name,
            "uuid", Codec.UUID.optional(), HeadProfile::uuid,
            "properties", Property.CODEC.list().optional(List.of()), HeadProfile::properties,
            HeadProfile::new);

    public HeadProfile(PlayerSkin playerSkin) {
        this(null, null, List.of(new Property("textures", playerSkin.textures(), playerSkin.signature())));
    }

    public @Nullable PlayerSkin skin() {
        for (Property property : properties) {
            if ("textures".equals(property.name)) {
                return new PlayerSkin(property.value, property.signature);
            }
        }
        return null;
    }

    public record Property(String name, String value, @Nullable String signature) {
        public static final NetworkBuffer.Type<Property> NETWORK_TYPE = NetworkBufferTemplate.template(
                STRING, Property::name,
                STRING, Property::value,
                STRING.optional(), Property::signature,
                Property::new);
        public static final Codec<Property> CODEC = StructCodec.struct(
                "name", Codec.STRING, Property::name,
                "value", Codec.STRING, Property::value,
                "signature", Codec.STRING.optional(), Property::signature,
                Property::new);
    }

}
