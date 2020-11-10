package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.location.RelativeBlockPosition;
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

    @Override
    public int getCorrectionResult(@NotNull String value) {
        final String[] split = value.split(" ");

        // Check if the value has enough element to be correct
        if (split.length != getNumberCount()) {
            return INVALID_NUMBER_COUNT_ERROR;
        }

        // Check if each element is correct
        for (String element : split) {
            if (!element.equals(RELATIVE_CHAR)) {
                try {
                    // Will throw the exception if not an integer
                    Integer.parseInt(element);
                } catch (NumberFormatException e) {
                    return INVALID_NUMBER_ERROR;
                }
            }
        }

        return SUCCESS;
    }

    @NotNull
    @Override
    public RelativeBlockPosition parse(@NotNull String value) {
        final String[] split = value.split(" ");

        BlockPosition blockPosition = new BlockPosition(0, 0, 0);
        boolean relativeX = false;
        boolean relativeY = false;
        boolean relativeZ = false;

        for (int i = 0; i < split.length; i++) {
            final String element = split[i];
            if (element.equals(RELATIVE_CHAR)) {
                if (i == 0) {
                    relativeX = true;
                } else if (i == 1) {
                    relativeY = true;
                } else if (i == 2) {
                    relativeZ = true;
                }
            } else {
                final int number = Integer.parseInt(element);
                if (i == 0) {
                    blockPosition.setX(number);
                } else if (i == 1) {
                    blockPosition.setY(number);
                } else if (i == 2) {
                    blockPosition.setZ(number);
                }
            }
        }

        return new RelativeBlockPosition(blockPosition, relativeX, relativeY, relativeZ);
    }

    @Override
    public int getConditionResult(@NotNull RelativeBlockPosition value) {
        return SUCCESS;
    }
}
