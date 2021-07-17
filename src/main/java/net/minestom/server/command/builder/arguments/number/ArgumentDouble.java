package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.ArgumentReader;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(String id) {
        super(id);
    }

    @NotNull
    @Override
    public Double parse(@NotNull ArgumentReader reader) throws ArgumentSyntaxException {
        // TODO: 2021. 07. 17. Use ArgumentReader for reading, low priority
        final String input = reader.readUnquotedString();
        try {
            final double value;
            {
                String parsed = parseValue(input);
                int radix = getRadix(input);
                if (radix != 10) {
                    value = (double) Long.parseLong(parsed, radix);
                } else {
                    value = Double.parseDouble(parsed);
                }
            }

            // Check range
            // TODO: 2021. 07. 17. Should these checks be here?
            if (hasMin && value < min) {
                throw new ArgumentSyntaxException("Input is lower than the minimum required value", input, RANGE_ERROR);
            }
            if (hasMax && value > max) {
                throw new ArgumentSyntaxException("Input is higher than the minimum required value", input, RANGE_ERROR);
            }

            return value;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ArgumentSyntaxException("Input is not a number/long", input, NOT_NUMBER_ERROR);
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = "brigadier:double";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeByte(getNumberProperties());
            if (this.hasMin())
                packetWriter.writeDouble(this.getMin());
            if (this.hasMax())
                packetWriter.writeDouble(this.getMax());
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Double<%s>", getId());
    }
}
