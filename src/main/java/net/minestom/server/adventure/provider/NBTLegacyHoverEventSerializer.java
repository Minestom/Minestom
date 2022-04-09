package net.minestom.server.adventure.provider;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minestom.server.adventure.MinestomAdventure;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

final class NBTLegacyHoverEventSerializer implements LegacyHoverEventSerializer {
    static final NBTLegacyHoverEventSerializer INSTANCE = new NBTLegacyHoverEventSerializer();

    private static final String ITEM_TYPE = "id", ITEM_COUNT = "Count", ITEM_TAG = "tag";
    private static final String ENTITY_TYPE = "type", ENTITY_NAME = "name", ENTITY_ID = "id";

    private NBTLegacyHoverEventSerializer() {
    }

    @Override
    public HoverEvent.@NotNull ShowItem deserializeShowItem(@NotNull Component input) throws IOException {
        final String raw = PlainTextComponentSerializer.plainText().serialize(input);
        try {
            // attempt the parse
            final NBT nbt = MinestomAdventure.NBT_CODEC.decode(raw);
            if (!(nbt instanceof NBTCompound contents)) throw new IOException("contents were not a compound");
            final NBTCompound tag = contents.getCompound(ITEM_TAG);

            // create the event
            return HoverEvent.ShowItem.of(
                    Key.key(Objects.requireNonNullElse(contents.getString(ITEM_TYPE), "")),
                    Objects.requireNonNullElse(contents.getByte(ITEM_COUNT), (byte) 1),
                    tag == null ? null : BinaryTagHolder.encode(tag, MinestomAdventure.NBT_CODEC)
            );
        } catch (final NBTException e) {
            throw new IOException(e);
        }
    }

    @Override
    public HoverEvent.@NotNull ShowEntity deserializeShowEntity(@NotNull Component input, Codec.Decoder<Component, String, ? extends RuntimeException> componentDecoder) throws IOException {
        final String raw = PlainTextComponentSerializer.plainText().serialize(input);
        try {
            final NBT nbt = MinestomAdventure.NBT_CODEC.decode(raw);
            if (!(nbt instanceof NBTCompound contents)) throw new IOException("contents were not a compound");

            return HoverEvent.ShowEntity.of(
                    Key.key(Objects.requireNonNullElse(contents.getString(ENTITY_TYPE), "")),
                    UUID.fromString(Objects.requireNonNullElse(contents.getString(ENTITY_ID), "")),
                    componentDecoder.decode(Objects.requireNonNullElse(contents.getString(ENTITY_NAME), ""))
            );
        } catch (NBTException e) {
            throw new IOException(e);
        }
    }

    @Override
    public @NotNull Component serializeShowItem(HoverEvent.@NotNull ShowItem input) throws IOException {
        AtomicReference<NBTException> exception = new AtomicReference<>(null);
        final NBTCompound tag = NBT.Compound(t -> {
            t.setString(ITEM_TYPE, input.item().asString());
            t.setByte(ITEM_COUNT, (byte) input.count());

            final BinaryTagHolder nbt = input.nbt();
            if (nbt != null) {
                try {
                    t.set(ITEM_TAG, nbt.get(MinestomAdventure.NBT_CODEC));
                } catch (NBTException e) {
                    exception.set(e);
                }
            }
        });

        if (exception.get() != null) {
            throw new IOException(exception.get());
        }

        return Component.text(MinestomAdventure.NBT_CODEC.encode(tag));
    }

    @Override
    public @NotNull Component serializeShowEntity(HoverEvent.@NotNull ShowEntity input, Codec.Encoder<Component, String, ? extends RuntimeException> componentEncoder) {
        final NBTCompound tag = NBT.Compound(t -> {
            t.setString(ENTITY_ID, input.id().toString());
            t.setString(ENTITY_TYPE, input.type().asString());

            final Component name = input.name();
            if (name != null) {
                t.setString(ENTITY_NAME, componentEncoder.encode(name));
            }
        });

        return Component.text(MinestomAdventure.NBT_CODEC.encode(tag));
    }
}
