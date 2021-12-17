package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentFloat extends ArgumentNumber<Float> {

    public ArgumentFloat(@NotNull String id) {
        super(id, "brigadier:float", BinaryWriter::writeFloat);
    }

    @Override
    public @NotNull Float parse(@NotNull StringReader input) throws CommandException {
        float value = input.readFloat();
        if (hasMin() && value < min){
            throw CommandException.ARGUMENT_FLOAT_LOW.generateException(input, min.toString(), Float.toString(value));
        }
        if (hasMax() && value > max){
            throw CommandException.ARGUMENT_FLOAT_BIG.generateException(input, max.toString(), Float.toString(value));
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("Float<%s>", getId());
    }
}
