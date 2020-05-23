package net.minestom.server.utils.validate;

import java.util.Objects;

public class Check {

    public static void notNull(Object object, String reason) {
        if (Objects.isNull(object))
            throw new NullPointerException(reason);
    }

    public static void argCondition(boolean condition, String reason) {
        if (condition)
            throw new IllegalArgumentException(reason);
    }

    public static void stateCondition(boolean condition, String reason) {
        if (condition)
            throw new IllegalStateException(reason);
    }

}
