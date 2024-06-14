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
public record Color(int red, int green, int blue) implements RGBLike {
    private static final int BIT_MASK = 0xff;

    public Color {
        Check.argCondition(!MathUtils.isBetween(red, 0, 255), "Red is not between 0-255: {0}", red);
        Check.argCondition(!MathUtils.isBetween(green, 0, 255), "Green is not between 0-255: {0}", green);
        Check.argCondition(!MathUtils.isBetween(blue, 0, 255), "Blue is not between 0-255: {0}", blue);
    }

    /**
     * Creates a color from an integer. This is done by reading each color component
     * from the lowest order 24 bits of the integer, and creating a color from those
     * components.
     *
     * @param rgb the integer
     */
    public Color(int rgb) {
        this((rgb >> 16) & BIT_MASK, (rgb >> 8) & BIT_MASK, rgb & BIT_MASK);
    }

    /**
     * Creates a color from an RGB-like color.
     *
     * @param rgbLike the color
     */
    public Color(@NotNull RGBLike rgbLike) {
        this(rgbLike.red(), rgbLike.green(), rgbLike.blue());
    }

    public @NotNull Color withRed(int red) {
        return new Color(red, green, blue);
    }

    public @NotNull Color withGreen(int green) {
        return new Color(red, green, blue);
    }

    public @NotNull Color withBlue(int blue) {
        return new Color(red, green, blue);
    }

    /**
     * Gets the color as an RGB integer.
     *
     * @return An integer representation of this color, as 0xRRGGBB
     */
    public int asRGB() {
        int rgb = red;
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
    public @NotNull Color mixWith(@NotNull RGBLike... colors) {
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
        return new Color(r, g, b);
    }

    @Deprecated
    public int getRed() {
        return this.red;
    }

    @Deprecated
    public int getGreen() {
        return this.green;
    }

    @Deprecated
    public int getBlue() {
        return this.blue;
    }
}
