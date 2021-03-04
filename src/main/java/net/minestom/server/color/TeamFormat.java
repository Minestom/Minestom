package net.minestom.server.color;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.Nullable;

/**
 * The format used for teams. Note that this is often referred to as "team color". This
 * is misleading as teams can also use text decoration like bold, italics, etc.
 */
public enum TeamFormat implements RGBLike {
    BLACK(NamedTextColor.BLACK),
    DARK_BLUE(NamedTextColor.DARK_BLUE),
    DARK_GREEN(NamedTextColor.DARK_GREEN),
    DARK_AQUA(NamedTextColor.DARK_AQUA),
    DARK_RED(NamedTextColor.DARK_RED),
    DARK_PURPLE(NamedTextColor.DARK_PURPLE),
    GOLD(NamedTextColor.GOLD),
    GRAY(NamedTextColor.GRAY),
    DARK_GRAY(NamedTextColor.DARK_GRAY),
    BLUE(NamedTextColor.BLUE),
    GREEN(NamedTextColor.GREEN),
    AQUA(NamedTextColor.AQUA),
    RED(NamedTextColor.RED),
    LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE),
    YELLOW(NamedTextColor.YELLOW),
    WHITE(NamedTextColor.WHITE),
    OBFUSCATED(TextDecoration.OBFUSCATED),
    BOLD(TextDecoration.BOLD),
    STRIKETHROUGH(TextDecoration.STRIKETHROUGH),
    UNDERLINE(TextDecoration.UNDERLINED),
    ITALIC(TextDecoration.ITALIC),
    RESET(null, null);

    private final TextDecoration decoration;
    private final TextColor color;

    TeamFormat(TextDecoration decoration) {
        this(decoration, null);
    }

    TeamFormat(TextColor color) {
        this(null, color);
    }

    TeamFormat(TextDecoration decoration, TextColor color) {
        this.decoration = decoration;
        this.color = color;
    }

    /**
     * Checks if this team color is a color.
     *
     * @return if it is a color
     */
    public boolean isColor() {
        return this.color != null;
    }

    /**
     * Gets the text color equivalent to this team color, if any.
     *
     * @return the text color
     */
    public @Nullable TextColor getTextColor() {
        return this.color;
    }

    /**
     * Gets the color equivalent to this team color, if any.
     *
     * @return the color
     */
    public @Nullable Color getColor() {
        return this.color == null ? null : new Color(this.color);
    }

    /**
     * Checks if this team color is a text decoration.
     *
     * @return if it is a decoration
     */
    public boolean isDecoration() {
        return this.decoration != null;
    }

    /**
     * Gets the text decoration equivalent to this team color, if any.
     *
     * @return the decoration
     */
    public @Nullable TextDecoration getDecoration() {
        return this.decoration;
    }

    /**
     * @throws IllegalStateException if this team format is not a color
     */
    @Override
    public int red() {
        if (!this.isColor()) {
            throw new IllegalStateException("This TeamFormat does not represent a color");
        }

        return this.color.red();
    }

    /**
     * @throws IllegalStateException if this team format is not a color
     */
    @Override
    public int green() {
        if (!this.isColor()) {
            throw new IllegalStateException("This TeamFormat does not represent a color");
        }

        return this.color.green();
    }

    /**
     * @throws IllegalStateException if this team format is not a color
     */
    @Override
    public int blue() {
        if (!this.isColor()) {
            throw new IllegalStateException("This TeamFormat does not represent a color");
        }

        return this.color.blue();
    }
}
