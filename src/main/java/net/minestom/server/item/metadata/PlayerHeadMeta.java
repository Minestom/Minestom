package net.minestom.server.item.metadata;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public record PlayerHeadMeta(TagReadable readable) implements ItemMetaView<PlayerHeadMeta.Builder> {
    private static final Tag<UUID> SKULL_OWNER = Tag.UUID("Id").path("SkullOwner");
    private static final Tag<PlayerSkin> SKIN = Tag.Structure("Properties", new TagSerializer<PlayerSkin>() {
        @Override
        public @Nullable PlayerSkin read(@NotNull TagReadable reader) {
            final String value = reader.getTag(Tag.String("Value"));
            final String signature = reader.getTag(Tag.String("Signature"));
            if (value == null || signature == null) return null;
            return new PlayerSkin(value, signature);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull PlayerSkin value) {
            writer.setTag(Tag.String("Value"), value.textures());
            writer.setTag(Tag.String("Signature"), value.signature());
        }
    }).path("SkullOwner");

    public UUID getSkullOwner() {
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
