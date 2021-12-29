package net.minestom.server.utils;

import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class MathUtils {

    private MathUtils() {
    }

    public static int square(int num) {
        return num * num;
    }

    public static float square(float num) {
        return num * num;
    }

    public static double square(double num) {
        return num * num;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        final long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        final long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (float) tmp / factor;
    }

    public static Direction getHorizontalDirection(float yawInDegrees) {
        // +45f gives a 90° angle for the direction (-1° and 1° are towards the same direction)
        int directionIndex = (int) Math.floor(((yawInDegrees + 45f) / 90f));
        if (directionIndex < 0) {
            directionIndex = (-directionIndex) % Direction.HORIZONTAL.length;
            directionIndex = Direction.HORIZONTAL.length - directionIndex;
        }
        directionIndex %= Direction.HORIZONTAL.length;
        return Direction.HORIZONTAL[directionIndex];
    }

    public static boolean isBetween(byte number, byte min, byte max) {
        return number >= min && number <= max;
    }

    public static boolean isBetween(int number, int min, int max) {
        return number >= min && number <= max;
    }

    public static boolean isBetween(double number, double min, double max) {
        return number >= min && number <= max;
    }

    public static boolean isBetween(float number, float min, float max) {
        return number >= min && number <= max;
    }

    public static boolean isBetweenUnordered(double number, double compare1, double compare2) {
        if (compare1 > compare2) {
            return isBetween(number, compare2, compare1);
        } else {
            return isBetween(number, compare1, compare2);
        }
    }

    public static boolean isBetweenUnordered(float number, float compare1, float compare2) {
        if (compare1 > compare2) {
            return isBetween(number, compare2, compare1);
        } else {
            return isBetween(number, compare1, compare2);
        }
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double mod(final double a, final double b) {
        return (a % b + b) % b;
    }

    public static int bitsToRepresent(int n) {
        Check.argCondition(n < 1, "n must be greater than 0");
        return Integer.SIZE - Integer.numberOfLeadingZeros(n);
    }

    public static double distance3Squared(double dx, double dy, double dz) {
        return dx * dx + dy * dy + dz * dz;
    }
    public static double distance3Squared(double x1, double y1, double z1, double x2, double y2, double z2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double dz = z2 - z1;
        return dx * dx + dy * dy + dz * dz;
    }
    public static double distance3Squared(@NotNull Point from, @NotNull Point to) {
        final double dx = to.x() - from.x();
        final double dy = to.y() - from.y();
        final double dz = to.z() - from.z();
        return dx * dx + dy * dy + dz * dz;
    }
    public static double distance3(double dx, double dy, double dz) {
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public static double distance3(double x1, double y1, double z1, double x2, double y2, double z2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public static double distance3(@NotNull Point from, @NotNull Point to) {
        final double dx = to.x() - from.x();
        final double dy = to.y() - from.y();
        final double dz = to.z() - from.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double distance2Squared(double dx, double dy) {
        return dx * dx + dy * dy;
    }
    public static double distance2Squared(double x1, double y1, double x2, double y2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    public static double distance2Squared(@NotNull Point from, @NotNull Point to) {
        final double dx = to.x() - from.x();
        final double dy = to.y() - from.y();
        return dx * dx + dy * dy;
    }
    public static double distance2(double dx, double dy) {
        return Math.sqrt(dx * dx + dy * dy);
    }
    public static double distance2(double x1, double y1, double x2, double y2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    public static double distance2(@NotNull Point from, @NotNull Point to) {
        final double dx = to.x() - from.x();
        final double dy = to.y() - from.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
