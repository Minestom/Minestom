package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PlayerHeadMeta(TagReadable readable) implements ItemMetaView<PlayerHeadMeta.Builder> {
    public static final Tag<UUID> SKULL_OWNER = Tag.UUID("Id").path("SkullOwner");
    public static final Tag<PlayerSkin> SKIN = Tag.Structure("Properties", new TagSerializer<PlayerSkin>() {
        private static final Tag<NBT> TEXTURES = Tag.NBT("textures");

        @Override
        public @Nullable PlayerSkin read(@NotNull TagReadable reader) {
            final NBT result = reader.getTag(TEXTURES);
            if (!(result instanceof NBTList)) return null;
            final NBTList<NBTCompound> textures = (NBTList<NBTCompound>) result;
            final NBTCompound texture = textures.get(0);
            final String value = texture.getString("Value");
            final String signature = texture.getString("Signature");
            return new PlayerSkin(value, signature);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull PlayerSkin playerSkin) {
            final String value = Objects.requireNonNullElse(playerSkin.textures(), "");
            final String signature = Objects.requireNonNullElse(playerSkin.signature(), "");
            NBTList<NBTCompound> textures = new NBTList<>(NBTType.TAG_Compound,
                    List.of(NBT.Compound(Map.of("Value", NBT.String(value), "Signature", NBT.String(signature)))));
            writer.setTag(TEXTURES, textures);
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
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

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
