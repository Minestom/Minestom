package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.utils.binary.BinaryWriter;

public class ArgumentLong extends ArgumentNumber<Long> {

    public ArgumentLong(String id) {
        super(id, "brigadier:long", Long::parseLong, Long::parseLong, BinaryWriter::writeLong, Long::compare);
    }

    @Override
    public String toString() {
        return String.format("Long<%s>", getId());
    }
}
