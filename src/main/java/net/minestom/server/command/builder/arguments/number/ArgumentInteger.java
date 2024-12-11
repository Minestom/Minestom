package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.network.NetworkBuffer;

public class ArgumentInteger extends ArgumentNumber<Integer> {

    public ArgumentInteger(String id) {
        super(id, ArgumentParserType.INTEGER, Integer::parseInt, Integer::parseInt,
                NetworkBuffer.INT, Integer::compare);
    }

    @Override
    public String toString() {
        return String.format("Integer<%s>", getId());
    }
}
