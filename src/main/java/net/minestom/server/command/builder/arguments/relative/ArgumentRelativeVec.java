package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for all the relative location arguments.
 */
abstract class ArgumentRelativeVec extends Argument<RelativeVec> {

    private static final char RELATIVE_CHAR = '~';
    private static final char LOCAL_CHAR = '^';

    private final int numberCount;
    private boolean adjustIntegers;

    public ArgumentRelativeVec(@NotNull String id, int numberCount) {
        super(id);
        this.numberCount = numberCount;
    }

    public boolean adjustIntegers() {
        return adjustIntegers;
    }

    public void adjustIntegers(boolean adjustIntegers) {
        this.adjustIntegers = adjustIntegers;
    }

    /**
     * Gets the amount of numbers that this relative location needs.
     *
     * @return the amount of coordinate required
     */
    public int getNumberCount() {
        return numberCount;
    }

    /**
     * Reads a {@link RelativeVec} from the provided string reader. This method reads a block position, meaning a
     * position with three arguments that disallows non-relative and non-local decimal values. For example, 1.2 is not
     * allowed, but ~1.2 or ^1.2 is because they're both relative or local. This method does not automatically round the
     * values down to the closest valid block position, so clients of this method must do it themselves.
     * @param reader the reader that the relative vector will be read from
     */
    public static @NotNull RelativeVec readRelativeBlockPos(@NotNull StringReader reader) {
        if (reader.canRead() && reader.peek() == LOCAL_CHAR) {
            return readLocalVec(reader, true);
        }

        boolean ra = startsWithRelative(reader);
        int a = reader.canRead() && !StringReader.isValidWhitespace(reader.peek()) ? reader.readInteger() : 0;
        skipSingularWhitespace(reader, true);

        boolean rb = startsWithRelative(reader);
        int b = reader.canRead() && !StringReader.isValidWhitespace(reader.peek()) ? reader.readInteger() : 0;
        skipSingularWhitespace(reader, true);

        boolean rc = startsWithRelative(reader);
        int c = reader.canRead() && !StringReader.isValidWhitespace(reader.peek()) ? reader.readInteger() : 0;

        if (ra || rb || rc) {
            return new RelativeVec(new Vec(a, b, c), RelativeVec.CoordinateType.RELATIVE, ra, rb, rc);
        }
        return new RelativeVec(new Vec(a, b, c), RelativeVec.CoordinateType.ABSOLUTE, false, false, false);
    }

    /**
     * Reads a {@link RelativeVec} from the provided string reader.
     * @param reader the reader that the relative vector will be read from
     * @param useY true if the vector should include the Y coordinate. When this is false, only the x and z values are
     *             read (as opposed to the x, y, and z values all being read) and the y value defaults to a relative
     *             value of zero.
     * @param adjustIntegers true if integer values without decimals should be increased by one (e.g. 5 -> 5.5, 5.0 ->
     *                       5.0, -4 -> -3.5).
     */
    public static @NotNull RelativeVec readRelativeVec(@NotNull StringReader reader, boolean useY, boolean adjustIntegers) throws CommandException {
        return reader.canRead() && reader.peek() == LOCAL_CHAR ? readLocalVec(reader, useY) : readOnlyRelativeVec(reader, useY, adjustIntegers);
    }

    private static @NotNull RelativeVec readOnlyRelativeVec(@NotNull StringReader reader, boolean useY, boolean adjustIntegers) {
        boolean ra = startsWithRelative(reader);
        double a = readRelativeValue(reader, adjustIntegers, ra);
        skipSingularWhitespace(reader, useY);

        boolean rb = startsWithRelative(reader);
        double b = readRelativeValue(reader, adjustIntegers, rb);

        if (!useY) {
            if (ra || rb) {
                return new RelativeVec(new Vec(a, b), RelativeVec.CoordinateType.RELATIVE, ra, true, rb);
            }
            return new RelativeVec(new Vec(a, b), RelativeVec.CoordinateType.ABSOLUTE, false, false, false);
        }

        skipSingularWhitespace(reader, true);

        boolean rc = startsWithRelative(reader);
        double c = readRelativeValue(reader, adjustIntegers, rc);

        if (ra || rb || rc) {
            return new RelativeVec(new Vec(a, b, c), RelativeVec.CoordinateType.RELATIVE, ra, rb, rc);
        }
        return new RelativeVec(new Vec(a, b, c), RelativeVec.CoordinateType.ABSOLUTE, false, false, false);
    }

    private static boolean startsWithRelative(@NotNull StringReader reader) {
        if (!reader.canRead()) {
            throw CommandException.ARGUMENT_POS_MISSING_DOUBLE.generateException(reader);
        }
        if (reader.peek() == LOCAL_CHAR) {
            throw CommandException.ARGUMENT_POS_MIXED.generateException(reader);
        }
        if (reader.peek() == RELATIVE_CHAR) {
            reader.skip();
            return true;
        }
        return false;
    }

    private static double readRelativeValue(@NotNull StringReader reader, boolean adjustIntegers, boolean isRelative) {
        if (!reader.canRead() || StringReader.isValidWhitespace(reader.peek())) {
            return 0;
        }
        if (!adjustIntegers || isRelative) {
            return reader.readDouble();
        }
        int pos = reader.position();
        double value = reader.readDouble();

        // Add 0.5 to the value if integers should be adjusted
        if (value % 1 == 0) {
            int index = reader.all().indexOf('.', pos);
            if (index != -1 && index < reader.position()) {
                value += 0.5;
            }
        }
        return value;
    }

    private static @NotNull RelativeVec readLocalVec(@NotNull StringReader reader, boolean useY) {
        double a = readLocalValue(reader);
        skipSingularWhitespace(reader, useY);

        double b = readLocalValue(reader);

        if (!useY) {
            return new RelativeVec(new Vec(a, b), RelativeVec.CoordinateType.LOCAL, false, false, false);
        }

        skipSingularWhitespace(reader, true);
        double c = readLocalValue(reader);

        return new RelativeVec(new Vec(a, b, c), RelativeVec.CoordinateType.LOCAL, false, false, false);
    }

    private static void skipSingularWhitespace(@NotNull StringReader reader, boolean useY) {
        if (!reader.canRead() || !StringReader.isValidWhitespace(reader.peek())) {
            throw (useY ? CommandException.ARGUMENT_POS3D_INCOMPLETE : CommandException.ARGUMENT_POS2D_INCOMPLETE).generateException(reader);
        }
        reader.skip();
    }

    private static double readLocalValue(@NotNull StringReader reader) {
        if (!reader.canRead()) {
            throw CommandException.ARGUMENT_POS_MISSING_DOUBLE.generateException(reader);
        }
        if (reader.peek() != LOCAL_CHAR) {
            throw CommandException.ARGUMENT_POS_MIXED.generateException(reader);
        }
        reader.skip();
        if (!reader.canRead() || StringReader.isValidWhitespace(reader.peek())) {
            return 0;
        }
        return reader.readDouble();
    }
}
