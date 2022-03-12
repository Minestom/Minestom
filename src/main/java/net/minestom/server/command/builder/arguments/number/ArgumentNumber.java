package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public abstract class ArgumentNumber<T extends Number> extends Argument<T> {

    public static final int NOT_NUMBER_ERROR = 1;
    public static final int TOO_LOW_ERROR = 2;
    public static final int TOO_HIGH_ERROR = 3;

    protected boolean hasMin, hasMax;
    protected T min, max;

    protected final String parserName;
    protected final BiConsumer<BinaryWriter, T> valueWriter;

    ArgumentNumber(@NotNull String id, @NotNull String parserName, @NotNull BiConsumer<BinaryWriter, T> valueWriter) {
        super(id);
        this.parserName = parserName;
        this.valueWriter = valueWriter;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = parserName;
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeByte(getNumberProperties());
            if (this.hasMin())
                valueWriter.accept(packetWriter, getMin());
            if (this.hasMax())
                valueWriter.accept(packetWriter, getMax());
        });

        nodeMaker.addNodes(argumentNode);
    }

    @NotNull
    public ArgumentNumber<T> min(@NotNull T value) {
        this.min = value;
        this.hasMin = true;
        return this;
    }

    @NotNull
    public ArgumentNumber<T> max(@NotNull T value) {
        this.max = value;
        this.hasMax = true;

        return this;
    }

    @NotNull
    public ArgumentNumber<T> between(@NotNull T min, @NotNull T max) {
        this.min = min;
        this.max = max;
        this.hasMin = true;
        this.hasMax = true;
        return this;
    }

    /**
     * Creates the byteflag based on the number's min/max existance.
     *
     * @return A byteflag for argument specification.
     */
    public byte getNumberProperties() {
        byte result = 0;
        if (this.hasMin())
            result |= 0x1;
        if (this.hasMax())
            result |= 0x2;
        return result;
    }

    /**
     * Gets if the argument has a minimum.
     *
     * @return true if the argument has a minimum
     */
    public boolean hasMin() {
        return hasMin;
    }

    /**
     * Gets the minimum value for this argument.
     *
     * @return the minimum of this argument
     */
    @NotNull
    public T getMin() {
        return min;
    }

    /**
     * Gets if the argument has a maximum.
     *
     * @return true if the argument has a maximum
     */
    public boolean hasMax() {
        return hasMax;
    }

    /**
     * Gets the maximum value for this argument.
     *
     * @return the maximum of this argument
     */
    @NotNull
    public T getMax() {
        return max;
    }

}
