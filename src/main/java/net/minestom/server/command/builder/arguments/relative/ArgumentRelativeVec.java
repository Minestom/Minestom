package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

import static net.minestom.server.utils.location.RelativeVec.CoordinateType.*;

/**
 * Common interface for all the relative location arguments.
 */
abstract class ArgumentRelativeVec extends Argument<RelativeVec> {

    private static final char RELATIVE_CHAR = '~';
    private static final char LOCAL_CHAR = '^';
    private static final Set<Character> MODIFIER_CHARS = Set.of(RELATIVE_CHAR, LOCAL_CHAR);

    public static final int INVALID_NUMBER_COUNT_ERROR = 1;
    public static final int INVALID_NUMBER_ERROR = 2;
    public static final int MIXED_TYPE_ERROR = 3;

    private final int numberCount;

    public ArgumentRelativeVec(@NotNull String id, int numberCount) {
        super(id, true);
        this.numberCount = numberCount;
    }

    abstract Function<String, ? extends Number> getRelativeNumberParser();

    abstract Function<String, ? extends Number> getAbsoluteNumberParser();

    @NotNull
    @Override
    public RelativeVec parse(@NotNull String input) throws ArgumentSyntaxException {
        final String[] split = input.split(StringUtils.SPACE);
        if (split.length != getNumberCount()) {
            throw new ArgumentSyntaxException("Invalid number of values", input, INVALID_NUMBER_COUNT_ERROR);
        }

        double[] coordinates = new double[split.length];
        boolean[] isRelative = new boolean[split.length];
        RelativeVec.CoordinateType type = null;
        for (int i = 0; i < split.length; i++) {
            final String element = split[i];
            try {
                final char modifierChar = element.charAt(0);
                if (MODIFIER_CHARS.contains(modifierChar)) {
                    isRelative[i] = true;

                    if (type == null) {
                        type = modifierChar == LOCAL_CHAR ? LOCAL : RELATIVE;
                    } else if (type != (modifierChar == LOCAL_CHAR ? LOCAL : RELATIVE)) {
                        throw new ArgumentSyntaxException("Cannot mix world & local coordinates (everything must either use ^ or not)", input, MIXED_TYPE_ERROR);
                    }

                    if (element.length() > 1) {
                        final String potentialNumber = element.substring(1);
                        coordinates[i] = getRelativeNumberParser().apply(potentialNumber).doubleValue();
                    }
                } else {
                    if (type == null) {
                        type = ABSOLUTE;
                    } else if (type == LOCAL) {
                        throw new ArgumentSyntaxException("Cannot mix world & local coordinates (everything must either use ^ or not)", input, MIXED_TYPE_ERROR);
                    }
                    coordinates[i] = getAbsoluteNumberParser().apply(element).doubleValue();
                }
            } catch (NumberFormatException e) {
                throw new ArgumentSyntaxException("Invalid number", input, INVALID_NUMBER_ERROR);
            }
        }

        return new RelativeVec(split.length == 3 ?
                new Vec(coordinates[0], coordinates[1], coordinates[2]) : new Vec(coordinates[0], coordinates[1]),
                type,
                isRelative[0], split.length == 3 && isRelative[1], isRelative[split.length == 3 ? 2 : 1]);
    }

    /**
     * Gets the amount of numbers that this relative location needs.
     *
     * @return the amount of coordinate required
     */
    public int getNumberCount() {
        return numberCount;
    }
}
