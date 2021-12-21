package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(@NotNull String id) {
        super(id, "brigadier:double", BinaryWriter::writeDouble);
    }

    @Override
    public @NotNull Double parse(@NotNull StringReader input) throws CommandException {
        int pos = input.position();
        double value = input.readDouble();
        if (hasMin() && value < min){
            throw CommandException.ARGUMENT_DOUBLE_LOW.generateException(input.all(), pos, min.toString(), Double.toString(value));
        }
        if (hasMax() && value > max){
            throw CommandException.ARGUMENT_DOUBLE_BIG.generateException(input.all(), pos, max.toString(), Double.toString(value));
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("Double<%s>", getId());
    }
}
