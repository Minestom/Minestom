package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

/**
 * Common super class for {@link ArgumentRelativeVec2} and {@link ArgumentRelativeVec3}.
 */
public abstract class ArgumentRelativeVec extends ArgumentRelative<RelativeVec> {

    public ArgumentRelativeVec(@NotNull String id, int numberCount) {
        super(id, numberCount);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        final String[] split = value.split(" ");

        // Check if the value has enough element to be correct
        if (split.length != getNumberCount()) {
            return INVALID_NUMBER_COUNT_ERROR;
        }

        // Check if each element is correct
        for (String element : split) {
            if (!element.startsWith(RELATIVE_CHAR)) {
                try {
                    // Will throw the exception if not a float
                    Float.parseFloat(element);
                } catch (NumberFormatException e) {
                    return INVALID_NUMBER_ERROR;
                }
            } else {
                if (element.length() > RELATIVE_CHAR.length()) {
                    try {
                        final String potentialNumber = element.substring(1);
                        // Will throw the exception if not a float
                        Float.parseFloat(potentialNumber);
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        return INVALID_NUMBER_ERROR;
                    }
                }
            }
        }

        return SUCCESS;
    }

    @Override
    public int getConditionResult(@NotNull RelativeVec value) {
        return SUCCESS;
    }

}
