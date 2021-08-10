package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.utils.binary.BinaryWriter;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(String id) {
        super(id, "brigadier:double", Double::parseDouble, ((s, radix) -> (double) Long.parseLong(s, radix)), BinaryWriter::writeDouble, Double::compare);
    }

    @Override
    public String toString() {
        return String.format("Double<%s>", getId());
    }
}
