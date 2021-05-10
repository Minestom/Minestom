package net.minestom.server.color;

import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

/**
 * Color values for dyes, wool and cloth items.
 */
public enum DyeColor implements RGBLike {
    WHITE(new Color(0xF9FFFE), new Color(0xF0F0F0)),
    ORANGE(new Color(0xF9801D), new Color(0xEB8844)),
    MAGENTA(new Color(0xC74EBD), new Color(0xC354CD)),
    LIGHT_BLUE(new Color(0x3AB3DA), new Color(0x6689D3)),
    YELLOW(new Color(0xFED83D), new Color(0xDECF2A)),
    LIME(new Color(0x80C71F), new Color(0x41CD34)),
    PINK(new Color(0xF38BAA), new Color(0xD88198)),
    GRAY(new Color(0x474F52), new Color(0x434343)),
    LIGHT_GRAY(new Color(0x9D9D97), new Color(0xABABAB)),
    CYAN(new Color(0x169C9C), new Color(0x287697)),
    PURPLE(new Color(0x8932B8), new Color(0x7B2FBE)),
    BLUE(new Color(0x3C44AA), new Color(0x253192)),
    BROWN(new Color(0x835432), new Color(0x51301A)),
    GREEN(new Color(0x5E7C16), new Color(0x3B511A)),
    RED(new Color(0xB02E26), new Color(0xB3312C)),
    BLACK(new Color(0x1D1D21), new Color(0x1E1B1B));

    private final Color color;
    private final Color firework;

    DyeColor(Color color, Color firework) {
        this.color = color;
        this.firework = firework;
    }

    /**
     * Gets the color that this dye represents.
     *
     * @return The {@link Color} that this dye represents
     */
    @NotNull
    public Color getColor() {
        return this.color;
    }

    /**
     * Gets the firework color that this dye represents.
     *
     * @return The {@link Color} that this dye represents
     */
    @NotNull
    public Color getFireworkColor() {
        return this.firework;
    }

    @Override
    public int red() {
        return this.color.red();
    }

    @Override
    public int green() {
        return this.color.green();
    }

    @Override
    public int blue() {
        return this.color.blue();
    }
}
