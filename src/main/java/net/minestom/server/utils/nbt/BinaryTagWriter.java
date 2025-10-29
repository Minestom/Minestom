package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;

import java.io.DataOutput;
import java.io.IOException;

// Based on net.kyori.adventure.nbt.BinaryTagWriterImpl licensed under the MIT license.
// https://github.com/KyoriPowered/adventure/blob/main/4/nbt/src/main/java/net/kyori/adventure/nbt/BinaryTagWriterImpl.java
public record BinaryTagWriter(DataOutput output) {

    static {
        BinaryTagTypes.COMPOUND.id(); // Force initialization
    }

    public void writeNameless(BinaryTag tag) throws IOException {
        //noinspection unchecked
        BinaryTagType<BinaryTag> type = (BinaryTagType<BinaryTag>) tag.type();
        output.writeByte(type.id());
        type.write(tag, output);
    }

    public void writeNamed(String name, BinaryTag tag) throws IOException {
        //noinspection unchecked
        BinaryTagType<BinaryTag> type = (BinaryTagType<BinaryTag>) tag.type();
        output.writeByte(type.id());
        output.writeUTF(name);
        type.write(tag, output);
    }
}
