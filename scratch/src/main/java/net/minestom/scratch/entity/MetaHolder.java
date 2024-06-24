package net.minestom.scratch.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class MetaHolder {
    private final int entityId;
    private final Consumer<ServerPacket.Play> consumer;
    private final Int2ObjectMap<Metadata.Entry<?>> entries = new Int2ObjectOpenHashMap<>();

    public MetaHolder(int entityId, Consumer<ServerPacket.Play> consumer) {
        this.entityId = entityId;
        this.consumer = consumer;
    }

    public MetaHolder(int entityId) {
        this(entityId, p -> {
        });
    }

    public <T> void set(MetadataDef.@NotNull Entry<T> entry, T value) {
        final int id = entry.index();
        switch (entry) {
            case MetadataDef.Entry.Index<T> v -> {
                final Metadata.Entry<?> result = v.function().apply(value);
                this.entries.put(id, result);
            }
            case MetadataDef.Entry.Mask mask -> this.entries.compute(id, (integer, currentEntry) -> {
                byte maskValue = currentEntry != null ? (byte) currentEntry.value() : 0;
                maskValue = setMaskBit(maskValue, (byte) mask.bitMask(), (Boolean) value);
                return Metadata.Byte(maskValue);
            });
        }
        this.consumer.accept(metaDataPacket());
    }

    public <T> T get(MetadataDef.@NotNull Entry<T> entry) {
        final int id = entry.index();
        final Metadata.Entry<?> value = this.entries.get(id);
        if (value == null) return entry.defaultValue();
        return switch (entry) {
            case MetadataDef.Entry.Index<T> v -> (T) value.value();
            case MetadataDef.Entry.Mask mask -> {
                final byte maskValue = (byte) value.value();
                yield (T) ((Boolean) getMaskBit(maskValue, (byte) mask.bitMask()));
            }
        };
    }

    public EntityMetaDataPacket metaDataPacket() {
        return new EntityMetaDataPacket(entityId, entries);
    }

    private boolean getMaskBit(byte maskValue, byte bit) {
        return (maskValue & bit) == bit;
    }

    private byte setMaskBit(byte mask, byte bit, boolean value) {
        return value ? (byte) (mask | bit) : (byte) (mask & ~bit);
    }
}
