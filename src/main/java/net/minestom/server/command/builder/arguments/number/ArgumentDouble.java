package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.network.NetworkBuffer;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(String id) {
        super(id, ArgumentParserType.DOUBLE, Double::parseDouble,
                NetworkBuffer.DOUBLE, Double::compare);
    }

    @Override
    public String toString() {
        return String.format("Double<%s>", getId());
    }
}
