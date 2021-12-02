package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.FixedStringReader;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.math.Range;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Abstract class used by {@link ArgumentIntRange} and {@link ArgumentFloatRange}.
 *
 * @param <T> the type of the range
 */
public abstract class ArgumentRange<T extends Range<N>, N extends Number> extends Argument<T> {

    public static final @NotNull String NUMBER_SEPARATOR = "..";

    public static final int FORMAT_ERROR = -1;
    private final N min;
    private final N max;
    private final Function<String, N> parser;
    private final String parserName;
    private final BiFunction<N, N, T> rangeConstructor;

    public ArgumentRange(@NotNull String id, String parserName, N min, N max, Function<String, N> parser, BiFunction<N, N, T> rangeConstructor) {
        super(id);
        this.min = min;
        this.max = max;
        this.parser = parser;
        this.parserName = parserName;
        this.rangeConstructor = rangeConstructor;
    }

    @NotNull
    @Override
    public T parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            final String[] split = input.split(Pattern.quote(".."), -1);

            if (split.length == 2) {
                final N min;
                final N max;
                if (split[0].length() == 0 && split[1].length() > 0) {
                    // Format ..NUMBER
                    min = this.min;
                    max = parser.apply(split[1]);
                } else if (split[0].length() > 0 && split[1].length() == 0) {
                    // Format NUMBER..
                    min = parser.apply(split[0]);
                    max = this.max;
                } else if (split[0].length() > 0) {
                    // Format NUMBER..NUMBER
                    min = parser.apply(split[0]);
                    max = parser.apply(split[1]);
                } else {
                    // Format ..
                    throw new ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR);
                }
                return rangeConstructor.apply(min, max);
            } else if (split.length == 1) {
                final N number = parser.apply(input);
                return rangeConstructor.apply(number, number);
            }
        } catch (NumberFormatException e2) {
            throw new ArgumentSyntaxException("Invalid number", input, FORMAT_ERROR);
        }
        throw new ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = parserName;

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    /**
     * Reads something that extends {@link Range} from the provided string reader.
     * @param reader the reader to read the range from
     * @param numberReader the function that parses the number from the provided string. The string is composed purely
     *                     of characters that are valid according to {@link StringReader#isValidNumber(int)}. The reader
     *                     is for throwing exceptions.
     * @param rangeConstructor the constructor for the range, taking a min and a max. This method will never provide two
     *                         null values into this BiFunction.
     * @param minGreaterThanMax a predicate, taking a min and a max (in that order), that returns true if the min is
     *                          larger than the max. The provided min and max will both not be null.
     * @return the range that was deserialized
     * @throws CommandException if the range could not be read
     */
    public static @NotNull <T extends Range<N>, N extends Number> T readNumberRange(@NotNull StringReader reader,
                                                                                    @NotNull BiFunction<String, FixedStringReader, N> numberReader,
                                                                                    @NotNull BiFunction<N, N, T> rangeConstructor,
                                                                                    @NotNull BiPredicate<N, N> minGreaterThanMax)
                                                                                    throws CommandException {
        if (!reader.canRead()) {
            throw CommandException.ARGUMENT_RANGE_EMPTY.generateException(reader);
        }

        N min = null, max = null;

        if (!hasSeparator(reader)) {
            min = numberReader.apply(readNumberString(reader), reader);
        }

        // If there is no more to read, we know that the string must have just a single number, which is equivalent to
        // a range with the same min and max.
        if (!reader.canRead() || StringReader.isValidWhitespace(reader.peek())) {
            if (min == null) {
                throw CommandException.ARGUMENT_RANGE_EMPTY.generateException(reader);
            }
            return rangeConstructor.apply(min, min);
        }

        if (hasSeparator(reader)) {
            reader.skip(NUMBER_SEPARATOR.length());
        } else {
            // If there isn't a separator, it means that there's some argument after it, which means we can just return
            // a value right here.
            if (min == null) {
                throw CommandException.ARGUMENT_RANGE_EMPTY.generateException(reader);
            }
            return rangeConstructor.apply(min, min);
        }

        if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
            return rangeConstructor.apply(min, null);
        }

        // Test if the string is empty to prevent bugs when the format ends with the number separator
        String readString = readNumberString(reader);
        if (!readString.isEmpty()) {
            max = numberReader.apply(readString, reader);
        }

        if (min == null && max == null) {
            throw CommandException.ARGUMENT_RANGE_EMPTY.generateException(reader);
        } else if (min != null && max != null) {
            if (minGreaterThanMax.test(min, max)) {
                throw CommandException.ARGUMENT_RANGE_SWAPPED.generateException(reader);
            }
        }
        return rangeConstructor.apply(min, max);
    }

    /**
     * Reads a string of characters from the reader that are theoretically valid to be parsed. This method stops reading
     * when there is a character that is not valid according to {@link StringReader#isValidNumber(int)} or the
     * {@link #NUMBER_SEPARATOR} is found.
     */
    private static @NotNull String readNumberString(@NotNull StringReader reader) {
        int startingPosition = reader.currentPosition();
        int positionInSeparator = 0;
        while (reader.canRead() && StringReader.isValidNumber(reader.peek())) {
            // Instead of just skipping the character, find out if it's part of the number separator
            char c = reader.nextChar();
            if (c == NUMBER_SEPARATOR.charAt(positionInSeparator)) {
                positionInSeparator++;
                if (positionInSeparator == NUMBER_SEPARATOR.length()) {
                    reader.currentPosition(reader.currentPosition() - NUMBER_SEPARATOR.length());
                    return reader.all().substring(startingPosition, reader.currentPosition());
                }
            } else {
                positionInSeparator = 0;
            }
        }
        return reader.all().substring(startingPosition, reader.currentPosition());
    }

    /**
     * Returns true if the reader's next characters are equal to the number separator. Importantly, this does not
     * actually read the characters, it just tests if they are the next ones.
     */
    private static boolean hasSeparator(@NotNull StringReader reader) {
        return reader.canRead(NUMBER_SEPARATOR.length()) &&
                reader.all().regionMatches(reader.currentPosition(), NUMBER_SEPARATOR, 0, NUMBER_SEPARATOR.length());
    }

}
