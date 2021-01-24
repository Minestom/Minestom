package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.location.RelativeBlockPosition;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link BlockPosition} with 3 integer numbers (x;y;z) which can take relative coordinates.
 * <p>
 * Example: 5 ~ -3
 */
public class ArgumentRelativeBlockPosition extends ArgumentRelative<RelativeBlockPosition> {

    public ArgumentRelativeBlockPosition(@NotNull String id) {
        super(id, 3);
    }

    @NotNull
    @Override
    public RelativeBlockPosition parse(@NotNull String input) throws ArgumentSyntaxException {
        final String[] split = input.split(StringUtils.SPACE);

        // Check if the value has enough element to be correct
        if (split.length != getNumberCount()) {
            throw new ArgumentSyntaxException("Invalid number of values", input, INVALID_NUMBER_COUNT_ERROR);
        }

        BlockPosition blockPosition = new BlockPosition(0, 0, 0);
        boolean relativeX = false;
        boolean relativeY = false;
        boolean relativeZ = false;

        for (int i = 0; i < split.length; i++) {
            final String element = split[i];
            if (element.startsWith(RELATIVE_CHAR)) {

                if (i == 0) {
                    relativeX = true;
                } else if (i == 1) {
                    relativeY = true;
                } else if (i == 2) {
                    relativeZ = true;
                }

                if (element.length() != RELATIVE_CHAR.length()) {
                    try {
                        final String potentialNumber = element.substring(1);
                        final int number = Integer.parseInt(potentialNumber);
                        if (i == 0) {
                            blockPosition.setX(number);
                        } else if (i == 1) {
                            blockPosition.setY(number);
                        } else if (i == 2) {
                            blockPosition.setZ(number);
                        }
                    } catch (NumberFormatException e) {
                        throw new ArgumentSyntaxException("Invalid number", input, INVALID_NUMBER_ERROR);
                    }
                }

            } else {
                try {
                    final int number = Integer.parseInt(element);
                    if (i == 0) {
                        blockPosition.setX(number);
                    } else if (i == 1) {
                        blockPosition.setY(number);
                    } else if (i == 2) {
                        blockPosition.setZ(number);
                    }
                } catch (NumberFormatException e) {
                    throw new ArgumentSyntaxException("Invalid number", input, INVALID_NUMBER_ERROR);
                }
            }
        }

        return new RelativeBlockPosition(blockPosition, relativeX, relativeY, relativeZ);
    }
}
