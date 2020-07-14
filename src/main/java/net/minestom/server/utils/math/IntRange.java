package net.minestom.server.utils.math;

public class IntRange {

    private int min, max;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public IntRange(int value) {
        this(value, value);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
