package net.minestom.server.color;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A general purpose class for representing colors.
 */
public class Color implements RGBLike {
    private static final int BIT_MASK = 0xff;

    private int red, green, blue;

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
    public Color(RGBLike rgbLike) {
        this(rgbLike.red(), rgbLike.green(), rgbLike.blue());
    }

    /**
     * Creates a color from red, green, and blue components.
     *
     * @param red   the red component
     * @param green the green component
     * @param blue  the blue component
     * @throws IllegalArgumentException if any component value is not between 0-255 (inclusive)
     */
    public Color(int red, int green, int blue) {
        Check.argCondition(!MathUtils.isBetween(red, 0, 255), "Red is not between 0-255: {0}", red);
        Check.argCondition(!MathUtils.isBetween(green, 0, 255), "Green is not between 0-255: {0}", green);
        Check.argCondition(!MathUtils.isBetween(blue, 0, 255), "Blue is not between 0-255: {0}", blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Gets the red component.
     *
     * @return red component, between 0-255 (inclusive)
     */
    public int getRed() {
        return this.red;
    }

    /**
     * Creates a new Color object with specified component
     *
     * @param red the red component, from 0 to 255
     */
    public void setRed(int red) {
        Check.argCondition(!MathUtils.isBetween(red, 0, 255), "Red is not between 0-255: {0}", red);
        this.red = red;
    }

    /**
     * Gets the green component
     *
     * @return green component, from 0 to 255
     */
    public int getGreen() {
        return this.green;
    }

    /**
     * Creates a new Color object with specified component
     *
     * @param green the red component, from 0 to 255
     */
    public void setGreen(int green) {
        Check.argCondition(!MathUtils.isBetween(green, 0, 255), "Green is not between 0-255: {0}", green);
        this.green = green;
    }

    /**
     * Gets the blue component
     *
     * @return blue component, from 0 to 255
     */
    public int getBlue() {
        return this.blue;
    }

    /**
     * Sets the blue component of this color.
     *
     * @param blue the red component, from 0 to 255
     */
    public void setBlue(int blue) {
        Check.argCondition(!MathUtils.isBetween(blue, 0, 255), "Blue is not between 0-255: {0}", blue);
        this.blue = blue;
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
    public void mixWith(@NotNull RGBLike... colors) {
        // store the current highest component
        int max = Math.max(Math.max(this.red, this.green), this.blue);

        // now combine all of the color components, adding to the max
        for (RGBLike color : colors) {
            this.red += color.red();
            this.green += color.green();
            this.blue += color.blue();
            max += Math.max(Math.max(color.red(), color.green()), color.blue());
        }

        // work out the averages
        float count = colors.length + 1;
        float averageRed = this.red / count;
        float averageGreen = this.green / count;
        float averageBlue = this.blue / count;
        float averageMax = max / count;

        // work out the scale factor
        float maximumOfAverages = Math.max(Math.max(averageRed, averageGreen), averageBlue);
        float gainFactor = averageMax / maximumOfAverages;

        // round and multiply
        this.red = Math.round(averageRed * gainFactor);
        this.blue = Math.round(averageBlue * gainFactor);
        this.green = Math.round(averageGreen * gainFactor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return red == color.red && green == color.green && blue == color.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

    @Override
    public int red() {
        return this.red;
    }

    @Override
    public int green() {
        return this.green;
    }

    @Override
    public int blue() {
        return this.blue;
    }
}
