package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentInteger extends ArgumentNumber<Integer> {

    public ArgumentInteger(@NotNull String id) {
        super(id, "brigadier:integer", BinaryWriter::writeInt);
    }

    @Override
    public @NotNull Integer parse(@NotNull StringReader input) throws CommandException {
        int pos = input.position();
        int value = input.readInteger();
        if (hasMin() && value < min){
            throw CommandException.ARGUMENT_INTEGER_LOW.generateException(input.all(), pos, min.toString(), Integer.toString(value));
        }
        if (hasMax() && value > max){
            throw CommandException.ARGUMENT_INTEGER_BIG.generateException(input.all(), pos, max.toString(), Integer.toString(value));
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("Integer<%s>", getId());
    }
}
