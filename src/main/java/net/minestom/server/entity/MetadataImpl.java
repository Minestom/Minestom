package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.UnknownNullability;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    @SuppressWarnings({"rawtypes", "unchecked"})
    record EntryImpl<T extends @UnknownNullability Object>(
            Metadata.Type<T> metadataType,
            T value
    ) implements Metadata.Entry<T> {
        @Override
        public int type() {
            return metadataType.id();
        }

        static final NetworkBuffer.Type<Metadata.Entry<?>> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Metadata.Entry<?> value) {
                final EntryImpl impl = (EntryImpl) value;
                buffer.write(VAR_INT, impl.metadataType.id());
                buffer.write(impl.metadataType.serializer(), impl.value);
            }

            @Override
            public Metadata.Entry<?> read(NetworkBuffer buffer) {
                final int id = buffer.read(VAR_INT);
                final Metadata.Type<?> type = Metadata.typeById(id);
                if (type == null) throw new UnsupportedOperationException("Unknown value type: " + id);
                return readEntry(buffer, type);
            }
        };

        private static <T extends @UnknownNullability Object> Metadata.Entry<T> readEntry(NetworkBuffer buffer, Metadata.Type<T> type) {
            return type.entry(buffer.read(type.serializer()));
        }
    }
}
