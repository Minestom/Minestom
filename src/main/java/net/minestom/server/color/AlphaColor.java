package net.minestom.server.color;

import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Nullable;

import java.util.HexFormat;
import java.util.Objects;

/**
 * A general purpose class for representing colors.
 * <p>
 * Colors must be in the range of 0-255.
 */
public final class AlphaColor extends Color implements ARGBLike {
    private static final int BIT_MASK = 0xff;

    public static final NetworkBuffer.Type<ARGBLike> NETWORK_TYPE = NetworkBuffer.INT.transform(
            AlphaColor::new, color -> fromARGBLike(color).asARGB());

    public static final Codec<ARGBLike> CODEC = Codec.INT.<ARGBLike>transform(AlphaColor::new, color -> fromARGBLike(color).asARGB())
            .orElse(Codec.FLOAT.list(4), floats -> new AlphaColor(floats.get(3), floats.get(0), floats.get(1), floats.get(2)));

    /**
     * Use {@link AlphaColor#RGBA_STRING_CODEC} or {@link AlphaColor#ARGB_STRING_CODEC} instead.
     * This codec uses RGBA.
     */
    @Deprecated
    public static final Codec<ARGBLike> STRING_CODEC = Codec.STRING.transform(
            hex -> (ARGBLike) Objects.requireNonNull(ShadowColor.fromHexString(hex)),
            color -> ShadowColor.shadowColor(color).asHexString()).orElse(CODEC);

    public static final Codec<ARGBLike> RGBA_STRING_CODEC = Codec.STRING.transform(
            hex -> (ARGBLike) Objects.requireNonNull(fromRGBAHexString(hex)),
            color -> String.format("#%08X", AlphaColor.fromARGBLike(color).asRGBA())).orElse(CODEC);

    public static final Codec<ARGBLike> ARGB_STRING_CODEC = Codec.STRING.transform(
            hex -> (ARGBLike) Objects.requireNonNull(fromARGBHexString(hex)),
            color -> String.format("#%08X", AlphaColor.fromARGBLike(color).asARGB())).orElse(CODEC);

    public static final AlphaColor WHITE = new AlphaColor(255, 255, 255, 255);
    public static final AlphaColor BLACK = new AlphaColor(255, 0, 0, 0);
    public static final AlphaColor TRANSPARENT = new AlphaColor(0, 0, 0, 0);

    public static AlphaColor fromARGBLike(ARGBLike argbLike) {
        if (argbLike instanceof AlphaColor alphaColor) return alphaColor;
        return new AlphaColor(argbLike.alpha(), argbLike.red(), argbLike.green(), argbLike.blue());
    }

    private final int alpha;

    public AlphaColor(float alpha, float red, float green, float blue) {
        this((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

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
    public AlphaColor(int alpha, RGBLike rgbLike) {
        this(alpha, rgbLike.red(), rgbLike.green(), rgbLike.blue());
    }

    @Override
    public AlphaColor withRed(int red) {
        return new AlphaColor(alpha(), red, green(), blue());
    }

    @Override
    public AlphaColor withGreen(int green) {
        return new AlphaColor(alpha(), red(), green, blue());
    }

    @Override
    public AlphaColor withBlue(int blue) {
        return new AlphaColor(alpha(), red(), green(), blue);
    }

    public AlphaColor withAlpha(int alpha) {
        return new AlphaColor(alpha, red(), green(), blue());
    }

    /**
     * Gets the color as an ARGB integer.
     *
     * @return An integer representation of this color, as 0xAARRGGBB
     */
    public int asARGB() {
        return (alpha << 24) + asRGB();
    }

    /**
     * Gets the color as an RGBA integer.
     *
     * @return An integer representation of this color, as 0xRRGGBBAA
     */
    public int asRGBA() {
        return (asRGB() << 8) + alpha;
    }

    /**
     * Attempt to parse a color from a {@code #}-prefixed hex string.
     * <p>
     * This string must be in the format {@code #RRGGBBAA}.
     *
     * @param hexRGBA the input value
     * @return a color if possible, or null if any components are invalid
     */
    public static @Nullable AlphaColor fromRGBAHexString(@Pattern("#[0-9a-fA-F]{8}") final String hexRGBA) {
        if (hexRGBA.length() != 9) return null;
        if (!hexRGBA.startsWith("#")) return null;

        try {
            int rgba = HexFormat.fromHexDigits(hexRGBA, 1, 9);
            int argb = Integer.rotateRight(rgba, 8);
            return new AlphaColor(argb);
        } catch (NumberFormatException _) {
            return null;
        }
    }

    /**
     * Attempt to parse a color from a {@code #}-prefixed hex string.
     * <p>
     * This string must be in the format {@code #AARRGGBB}.
     *
     * @param hexARGB the input value
     * @return a color if possible, or null if any components are invalid
     */
    public static @Nullable AlphaColor fromARGBHexString(@Pattern("#[0-9a-fA-F]{8}") final String hexARGB) {
        if (hexARGB.length() != 9) return null;
        if (!hexARGB.startsWith("#")) return null;

        try {
            int argb = HexFormat.fromHexDigits(hexARGB, 1, 9);
            return new AlphaColor(argb);
        } catch (NumberFormatException _) {
            return null;
        }
    }

    @Override
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
