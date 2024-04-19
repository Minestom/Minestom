package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record HeadProfile(@Nullable String name, @Nullable UUID uuid, @NotNull List<Property> properties) {
    public static final HeadProfile EMPTY = new HeadProfile(null, null, List.of());

    public static final NetworkBuffer.Type<HeadProfile> NETWORK_TYPE = new NetworkBuffer.Type<HeadProfile>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, HeadProfile value) {
            buffer.writeOptional(NetworkBuffer.STRING, value.name);
            buffer.writeOptional(NetworkBuffer.UUID, value.uuid);
            buffer.writeCollection(Property.NETWORK_TYPE, value.properties);
        }

        @Override
        public HeadProfile read(@NotNull NetworkBuffer buffer) {
            return new HeadProfile(buffer.readOptional(NetworkBuffer.STRING), buffer.readOptional(NetworkBuffer.UUID), buffer.readCollection(Property.NETWORK_TYPE, Short.MAX_VALUE));
        }
    };
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
                if (!profile.properties.isEmpty()) builder.put("properties", Property.NBT_LIST_TYPE.write(profile.properties));
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
        public static final NetworkBuffer.Type<Property> NETWORK_TYPE = new NetworkBuffer.Type<Property>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Property value) {
                buffer.write(NetworkBuffer.STRING, value.name);
                buffer.write(NetworkBuffer.STRING, value.value);
                buffer.writeOptional(NetworkBuffer.STRING, value.signature);
            }

            @Override
            public Property read(@NotNull NetworkBuffer buffer) {
                return new Property(buffer.read(NetworkBuffer.STRING), buffer.read(NetworkBuffer.STRING), buffer.readOptional(NetworkBuffer.STRING));
            }
        };
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
