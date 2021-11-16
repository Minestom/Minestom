package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentLong extends ArgumentNumber<Long> {

    public ArgumentLong(String id) {
        super(id, "brigadier:long", Long::parseLong, Long::parseLong, BinaryWriter::writeLong, Long::compare);
    }

    @Override
    public @NotNull Long parse(@NotNull StringReader input) throws CommandException {
        long value = input.readLong();
        if (hasMin() && value < min){
            throw CommandException.ARGUMENT_LONG_LOW.generateException(input, min.toString(), Long.toString(value));
        }
        if (hasMax() && value > max){
            throw CommandException.ARGUMENT_LONG_BIG.generateException(input, max.toString(), Long.toString(value));
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("Long<%s>", getId());
    }
}
