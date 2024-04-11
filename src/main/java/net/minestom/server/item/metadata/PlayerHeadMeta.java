package net.minestom.server.item.metadata;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentMap;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PlayerHeadMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<PlayerHeadMeta.Builder> {
    public static final Tag<UUID> SKULL_OWNER = Tag.UUID("Id").path("SkullOwner");
    public static final Tag<PlayerSkin> SKIN = Tag.Structure("Properties", new TagSerializer<PlayerSkin>() {
        private static final Tag<BinaryTag> TEXTURES = Tag.NBT("textures");

        @Override
        public @Nullable PlayerSkin read(@NotNull TagReadable reader) {
            final BinaryTag result = reader.getTag(TEXTURES);
            if (!(result instanceof ListBinaryTag textures)) return null;
            final CompoundBinaryTag texture = textures.getCompound(0);
            final String value = texture.getString("Value");
            final String signature = texture.getString("Signature");
            return new PlayerSkin(value, signature);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull PlayerSkin playerSkin) {
            final String value = Objects.requireNonNullElse(playerSkin.textures(), "");
            final String signature = Objects.requireNonNullElse(playerSkin.signature(), "");
            writer.setTag(TEXTURES, ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                    CompoundBinaryTag.builder().putString("Value", value).putString("Signature", signature).build()
            )));
        }
    }).path("SkullOwner");

    public @Nullable UUID getSkullOwner() {
        return getTag(SKULL_OWNER);
    }

    public @Nullable PlayerSkin getPlayerSkin() {
        return getTag(SKIN);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    public record Builder(@NotNull ItemComponentMap.Builder components) implements ItemMetaView.Builder {

        public Builder skullOwner(@Nullable UUID skullOwner) {
            setTag(SKULL_OWNER, skullOwner);
            return this;
        }

        public Builder playerSkin(@Nullable PlayerSkin playerSkin) {
            setTag(SKIN, playerSkin);
            return this;
        }
    }
}
