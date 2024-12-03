package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.UUID;

public record HeadProfile(@Nullable String name, @Nullable UUID uuid, @NotNull List<Property> properties) {
    public static final HeadProfile EMPTY = new HeadProfile(null, null, List.of());

    public static final NetworkBuffer.Type<HeadProfile> NETWORK_TYPE = NetworkBufferTemplate.template(
            STRING.optional(), HeadProfile::name,
            UUID.optional(), HeadProfile::uuid,
            Property.NETWORK_TYPE.list(Short.MAX_VALUE), HeadProfile::properties,
            HeadProfile::new
    );

    public static final BinaryTagSerializer<HeadProfile> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new HeadProfile(
                    tag.get("name") instanceof StringBinaryTag string ? string.value() : null,
                    tag.get("uuid") instanceof IntArrayBinaryTag intArray ? BinaryTagSerializer.UUID.read(intArray) : null,
                    Property.NBT_LIST_TYPE.read(tag.getList("properties"))
            ),
            profile -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                if (profile.name != null) builder.putString("name", profile.name);
                if (profile.uuid != null) builder.put("uuid", BinaryTagSerializer.UUID.write(profile.uuid));
                if (!profile.properties.isEmpty())
                    builder.put("properties", Property.NBT_LIST_TYPE.write(profile.properties));
                return builder.build();
            }
    );

    public HeadProfile(@NotNull PlayerSkin playerSkin) {
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

    public record Property(@NotNull String name, @NotNull String value, @Nullable String signature) {
        public static final NetworkBuffer.Type<Property> NETWORK_TYPE = NetworkBufferTemplate.template(
                STRING, Property::name,
                STRING, Property::value,
                STRING.optional(), Property::signature,
                Property::new
        );

        public static final BinaryTagSerializer<Property> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Property(tag.getString("name"), tag.getString("value"),
                        tag.get("signature") instanceof StringBinaryTag signature ? signature.value() : null),
                property -> {
                    CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                    builder.putString("name", property.name);
                    builder.putString("value", property.value);
                    if (property.signature != null) builder.putString("signature", property.signature);
                    return builder.build();
                }
        );
        public static final BinaryTagSerializer<List<Property>> NBT_LIST_TYPE = NBT_TYPE.list();
    }

}
