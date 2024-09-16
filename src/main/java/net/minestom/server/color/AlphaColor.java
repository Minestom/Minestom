package net.minestom.server.color;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A general purpose class for representing colors.
 * <p>
 * Colors must be in the range of 0-255.
 */
public final class AlphaColor extends Color {
    private static final int BIT_MASK = 0xff;

    public static final NetworkBuffer.Type<AlphaColor> NETWORK_TYPE = NetworkBuffer.INT.transform(AlphaColor::new, AlphaColor::asARGB);
    private final int alpha;

    public AlphaColor(int alpha, int red, int green, int blue) {
        super(red, green, blue);
        Check.argCondition(!MathUtils.isBetween(alpha, 0, 255), "Alpha is not between 0-255: {0}", alpha);
        this.alpha = alpha;
    }

    /**
     * Creates an alpha color from an integer. This is done by reading each color component
     * from the lowest order 32 bits of the integer, and creating a color from those
     * components.
     *
     * @param argb the integer
     */
    public AlphaColor(int argb) {
        this((argb >> 24) & BIT_MASK, (argb >> 16) & BIT_MASK, (argb >> 8) & BIT_MASK, argb & BIT_MASK);
    }

    /**
     * Creates a color from an RGB-like color.
     *
     * @param rgbLike the color
     */
    public AlphaColor(int alpha, @NotNull RGBLike rgbLike) {
        this(alpha, rgbLike.red(), rgbLike.green(), rgbLike.blue());
    }

    @Override
    public @NotNull AlphaColor withRed(int red) {
        return new AlphaColor(alpha(), red, green(), blue());
    }

    @Override
    public @NotNull AlphaColor withGreen(int green) {
        return new AlphaColor(alpha(), red(), green, blue());
    }

    @Override
    public @NotNull AlphaColor withBlue(int blue) {
        return new AlphaColor(alpha(), red(), green(), blue);
    }

    public @NotNull AlphaColor withAlpha(int alpha) {
        return new AlphaColor(alpha, red(), green(), blue());
    }

    /**
     * Gets the color as an RGB integer.
     *
     * @return An integer representation of this color, as 0xRRGGBB
     */
    public int asARGB() {
        return (alpha << 24) + asRGB();
    }

    public int alpha() {
        return alpha;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AlphaColor) obj;
        return this.alpha == that.alpha &&
                red() == that.red() &&
                this.green() == that.green() &&
                this.blue() == that.blue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(alpha, red(), green(), blue());
    }

    @Override
    public String toString() {
        return "AlphaColor[" +
                "alpha=" + alpha + ", " +
                "red=" + red() + ", " +
                "green=" + green() + ", " +
                "blue=" + blue() + ']';
    }

}
