package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.network.NetworkBuffer;

public class ArgumentFloat extends ArgumentNumber<Float> {

    public ArgumentFloat(String id) {
        super(id, "brigadier:float", Float::parseFloat, (s, radix) -> (float) Integer.parseInt(s, radix),
                NetworkBuffer.FLOAT, Float::compare);
    }

    @Override
    public String toString() {
        return String.format("Float<%s>", getId());
    }
}
