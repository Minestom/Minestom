package net.minestom.server.network.player;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ApiStatus.Experimental
public record GameProfile(@NotNull UUID uuid, @NotNull String name,
                          @NotNull List<@NotNull Property> properties) implements Writeable {
    public GameProfile {
        if (name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        if (name.length() > 16)
            throw new IllegalArgumentException("Name length cannot be greater than 16 characters");
        properties = List.copyOf(properties);
    }

    public GameProfile(@NotNull BinaryReader reader) {
        this(reader.readUuid(), reader.readSizedString(16), reader.readVarIntList(Property::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeSizedString(name);
        writer.writeVarIntList(properties, BinaryWriter::write);
    }

    public record Property(@NotNull String name, @NotNull String value,
                           @Nullable String signature) implements Writeable {
        public Property(@NotNull String name, @NotNull String value) {
            this(name, value, null);
        }

        public Property(@NotNull BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    reader.readBoolean() ? reader.readSizedString() : null);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.writeSizedString(value);
            writer.writeBoolean(signature != null);
            if (signature != null) writer.writeSizedString(signature);
        }
    }
}
