package net.minestom.server.color;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * A general purpose class for representing colors.
 * <p>
 * Colors must be in the range of 0-255.
 */
public record ARGBColor(int alpha, int red, int green, int blue) implements RGBLike {
    private static final int BIT_MASK = 0xff;

    public ARGBColor {
        Check.argCondition(!MathUtils.isBetween(alpha, 0, 255), "Alpha is not between 0-255: {0}", alpha);
        Check.argCondition(!MathUtils.isBetween(red, 0, 255), "Red is not between 0-255: {0}", red);
        Check.argCondition(!MathUtils.isBetween(green, 0, 255), "Green is not between 0-255: {0}", green);
        Check.argCondition(!MathUtils.isBetween(blue, 0, 255), "Blue is not between 0-255: {0}", blue);
    }

    /**
     * Creates a color from an integer. This is done by reading each color component
     * from the lowest order 24 bits of the integer, and creating a color from those
     * components.
     *
     * @param argb the integer
     */
    public ARGBColor(int argb) {
        this((argb >> 24) & BIT_MASK, (argb >> 16) & BIT_MASK, (argb >> 8) & BIT_MASK, argb & BIT_MASK);
    }

    /**
     * Creates a color from an RGB-like color.
     *
     * @param rgbLike the color
     */
    public ARGBColor(@NotNull RGBLike rgbLike) {
        this(255, rgbLike.red(), rgbLike.green(), rgbLike.blue());
    }

    public @NotNull ARGBColor withAlpha(int alpha) {
        return new ARGBColor(alpha, red, green, blue);
    }

    public @NotNull ARGBColor withRed(int red) {
        return new ARGBColor(alpha, red, green, blue);
    }

    public @NotNull ARGBColor withGreen(int green) {
        return new ARGBColor(alpha, red, green, blue);
    }

    public @NotNull ARGBColor withBlue(int blue) {
        return new ARGBColor(alpha, red, green, blue);
    }

    /**
     * Gets the color as an RGB integer.
     *
     * @return An integer representation of this color, as 0xRRGGBB
     */
    public int asARGB() {
        int rgb = alpha;
        rgb = (rgb << 8) + red;
        rgb = (rgb << 8) + green;
        return (rgb << 8) + blue;
    }

    /**
     * Mixes this color with a series of other colors, as if they were combined in a
     * crafting table. This function works out the average of each RGB component and then
     * multiplies the components by a scale factor that is calculated from the average
     * of all maximum values divided by the maximum of each average value. This is how
     * Minecraft mixes colors.
     *
     * @param colors the colors
     */
    public @NotNull ARGBColor mixWith(@NotNull RGBLike... colors) {
        int r = red, g = green, b = blue;

        // store the current highest component
        int max = Math.max(Math.max(r, g), b);

        // now combine all the color components, adding to the max
        for (RGBLike color : colors) {
            r += color.red();
            g += color.green();
            b += color.blue();
            max += Math.max(Math.max(color.red(), color.green()), color.blue());
        }

        // work out the averages
        float count = colors.length + 1;
        float averageRed = r / count;
        float averageGreen = g / count;
        float averageBlue = b / count;
        float averageMax = max / count;

        // work out the scale factor
        float maximumOfAverages = Math.max(Math.max(averageRed, averageGreen), averageBlue);
        float gainFactor = averageMax / maximumOfAverages;

        // round and multiply
        r = Math.round(averageRed * gainFactor);
        g = Math.round(averageGreen * gainFactor);
        b = Math.round(averageBlue * gainFactor);
        return new ARGBColor(alpha, r, g, b);
    }
}
