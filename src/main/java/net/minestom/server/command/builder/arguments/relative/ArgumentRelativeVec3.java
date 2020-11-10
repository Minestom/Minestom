package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.utils.Vector;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

public class ArgumentRelativeVec3 extends ArgumentRelative<RelativeVec> {

    public ArgumentRelativeVec3(@NotNull String id) {
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
                    // Will throw the exception if not a float
                    Float.parseFloat(element);
                } catch (NumberFormatException e) {
                    return INVALID_NUMBER_ERROR;
                }
            }
        }

        return SUCCESS;
    }

    @NotNull
    @Override
    public RelativeVec parse(@NotNull String value) {
        final String[] split = value.split(" ");

        Vector vector = new Vector();
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
                final float number = Float.parseFloat(element);
                if (i == 0) {
                    vector.setX(number);
                } else if (i == 1) {
                    vector.setY(number);
                } else if (i == 2) {
                    vector.setZ(number);
                }
            }
        }

        return new RelativeVec(vector, relativeX, relativeY, relativeZ);
    }

    @Override
    public int getConditionResult(@NotNull RelativeVec value) {
        return SUCCESS;
    }
}
