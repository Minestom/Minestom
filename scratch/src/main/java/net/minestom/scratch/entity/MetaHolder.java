package net.minestom.scratch.entity;

import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MetaHolder {
    private final int entityId;
    private final Consumer<ServerPacket.Play> consumer;
    private final Map<Integer, Metadata.Entry<?>> entries = new HashMap<>();

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
        final Metadata.Entry<?> result = switch (entry) {
            case MetadataDef.Entry.Index<T> v -> {
                var tmp = v.function().apply(value);
                this.entries.put(id, tmp);
                yield tmp;
            }
            case MetadataDef.Entry.Mask mask -> {
                byte maskValue;
                Metadata.Entry<?> currentEntry = this.entries.get(id);
                if (currentEntry != null) {
                    maskValue = (byte) currentEntry.value();
                } else {
                    maskValue = 0;
                }
                maskValue = setMaskBit(maskValue, (byte) mask.bitMask(), (Boolean) value);
                yield Metadata.Byte(maskValue);
            }
        };
        this.entries.put(id, result);
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
        if (value) {
            mask |= bit;
        } else {
            mask &= ~bit;
        }
        return mask;
    }
}
