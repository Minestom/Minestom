package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ArgumentNumber<T extends Number> extends Argument<T> {

    public static final int NOT_NUMBER_ERROR = 1;
    public static final int TOO_LOW_ERROR = 2;
    public static final int TOO_HIGH_ERROR = 3;

    protected boolean hasMin, hasMax;
    protected T min, max;

    protected final String parserName;
    protected final BiFunction<String, Integer, T> radixParser;
    protected final Function<String, T> parser;
    protected final BiConsumer<BinaryWriter, T> propertiesWriter;
    protected final Comparator<T> comparator;

    ArgumentNumber(@NotNull String id, String parserName, Function<String, T> parser,
                   BiFunction<String, Integer, T> radixParser, BiConsumer<BinaryWriter, T> propertiesWriter,
                   Comparator<T> comparator) {
        super(id);
        this.parserName = parserName;
        this.radixParser = radixParser;
        this.parser = parser;
        this.propertiesWriter = propertiesWriter;
        this.comparator = comparator;
    }

    @Override
    public @NotNull T parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            final T value;
            final int radix = getRadix(input);
            if (radix == 10) {
                value = parser.apply(parseValue(input));
            } else {
                value = radixParser.apply(parseValue(input), radix);
            }

            // Check range
            if (hasMin && comparator.compare(value, min) < 0) {
                throw new ArgumentSyntaxException("Input is lower than the minimum allowed value", input, TOO_LOW_ERROR);
            }
            if (hasMax && comparator.compare(value, max) > 0) {
                throw new ArgumentSyntaxException("Input is higher than the maximum allowed value", input, TOO_HIGH_ERROR);
            }

            return value;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ArgumentSyntaxException("Input is not a number, or it's invalid for the given type", input, NOT_NUMBER_ERROR);
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = parserName;
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeByte(getNumberProperties());
            if (this.hasMin())
                propertiesWriter.accept(packetWriter, getMin());
            if (this.hasMax())
                propertiesWriter.accept(packetWriter, getMax());
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
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

    @NotNull
    protected String parseValue(@NotNull String value) {
        if (value.startsWith("0b")) {
            value = value.replaceFirst(Pattern.quote("0b"), "");
        } else if (value.startsWith("0x")) {
            value = value.replaceFirst(Pattern.quote("0x"), "");
        } else if (value.toLowerCase().contains("e")) {
            value = removeScientificNotation(value);
        }
        // TODO number suffix support (k,m,b,t)
        return value;
    }

    protected int getRadix(@NotNull String value) {
        if (value.startsWith("0b")) {
            return 2;
        } else if (value.startsWith("0x")) {
            return 16;
        }
        return 10;
    }

    @Nullable
    protected String removeScientificNotation(@NotNull String value) {
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
