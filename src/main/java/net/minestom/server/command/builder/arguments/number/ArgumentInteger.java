package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentInteger extends ArgumentNumber<Integer> {

    public ArgumentInteger(String id) {
        super(id);
    }

    @NotNull
    @Override
    public Integer parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            final int value = Integer.parseInt(parseValue(input), getRadix(input));

            // Check range
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

        argumentNode.parser = "brigadier:integer";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeByte(getNumberProperties());
            if (this.hasMin())
                packetWriter.writeInt(this.getMin());
            if (this.hasMax())
                packetWriter.writeInt(this.getMax());
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

}
