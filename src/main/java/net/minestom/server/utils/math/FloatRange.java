package net.minestom.server.utils.math;

public class FloatRange {

    private float min, max;

    public FloatRange(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public FloatRange(float value) {
        this(value, value);
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

}
