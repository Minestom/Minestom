package net.minestom.server.utils;

// https://github.com/ai/easings.net/blob/master/src/easings/easingsFunctions.ts
public final class Ease {
    private static final float c1 = 1.70158f;
    private static final float c2 = c1 * 1.525f;
    private static final float c3 = c1 + 1;
    private static final float c4 = (float) (2 * Math.PI) / 3;
    private static final float c5 = (float) (2 * Math.PI) / 4.5f;

    public static float constant(float x) {
        return 0f;
    }

    public static float linear(float x) {
        return x;
    }

    public static float inQuad(float x) {
        return x * x;
    }

    public static float outQuad(float x) {
        return 1 - (1 - x) * (1 - x);
    }

    public static float inOutQuad(float x) {
        if (x < 0.5) return 2 * x * x;
        return 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
    }

    public static float inCubic(float x) {
        return x * x * x;
    }

    public static float outCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }

    public static float inOutCubic(float x) {
        if (x < 0.5) return 4 * x * x * x;
        return 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
    }

    public static float inQuart(float x) {
        return x * x * x * x;
    }

    public static float outQuart(float x) {
        return 1 - (float) Math.pow(1 - x, 4);
    }

    public static float inOutQuart(float x) {
        if (x < 0.5) return 8 * x * x * x * x;
        return 1 - (float) Math.pow(-2 * x + 2, 4) / 2;
    }

    public static float inQuint(float x) {
        return x * x * x * x * x;
    }

    public static float outQuint(float x) {
        return 1 - (float) Math.pow(1 - x, 5);
    }

    public static float inOutQuint(float x) {
        if (x < 0.5) return 16 * x * x * x * x * x;
        return 1 - (float) Math.pow(-2 * x + 2, 5) / 2;
    }

    public static float inSine(float x) {
        return 1 - (float) Math.cos((x * Math.PI) / 2);
    }

    public static float outSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2);
    }

    public static float inOutSine(float x) {
        return (float) -(Math.cos(Math.PI * x) - 1) / 2;
    }

    public static float inExpo(float x) {
        if (x == 0) return 0;
        return (float) Math.pow(2, 10 * x - 10);
    }

    public static float outExpo(float x) {
        if (x == 1) return 1;
        return 1 - (float) Math.pow(2, -10 * x);
    }

    public static float inOutExpo(float x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        if (x < 0.5) return (float) Math.pow(2, 20 * x - 10) / 2;
        return (2 - (float) Math.pow(2, -20 * x + 10)) / 2;
    }

    public static float inCirc(float x) {
        return 1 - (float) Math.sqrt(1 - Math.pow(x, 2));
    }

    public static float outCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    public static float inOutCirc(float x) {
        if (x < 0.5) return (float) (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2;
        return (float) (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
    }

    public static float inBack(float x) {
        return c3 * x * x * x - c1 * x * x;
    }

    public static float outBack(float x) {
        return 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
    }

    public static float inOutBack(float x) {
        return 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
    }

    public static float inElastic(float x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        return (float) (-Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4));
    }

    public static float outElastic(float x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        return (float) Math.pow(2, -10 * x) * (float) Math.sin((x * 10 - 0.75) * c4) + 1;
    }

    public static float inOutElastic(float x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        if (x < 0.5) return (float) -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2;
        return (float) (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1;
    }

    public static float inBounce(float x) {
        return 1 - outBounce(1 - x);
    }

    public static float outBounce(float x) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            x -= 1.5f / d1;
            return n1 * x * x + 0.75f;
        } else if (x < 2.5 / d1) {
            x -= 2.25f / d1;
            return n1 * x * x + 0.9375f;
        } else {
            x -= 2.625f / d1;
            return n1 * x * x + 0.984375f;
        }
    }

    public static float inOutBounce(float x) {
        if (x < 0.5) return (1 - outBounce(1 - 2 * x)) / 2;
        return (1 + outBounce(2 * x - 1)) / 2;
    }

}
