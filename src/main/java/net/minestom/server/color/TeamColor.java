package net.minestom.server.color;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.Nullable;

/**
 * The format used for teams. Note that this is often referred to as "team color". This
 * is misleading as teams can also use text decoration like bold, italics, etc.
 */
public enum TeamColor implements RGBLike {
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
    WHITE(NamedTextColor.WHITE);

    private final TextColor color;

    TeamColor(TextColor color) {
        this.color = color;
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
     * Gets the ID number of this team color.
     *
     * @return the id number
     */
    public int getId() {
        return this.ordinal();
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
