package net.minestom.server.color;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public enum TeamColor implements RGBLike {
    BLACK("black", NamedTextColor.BLACK),
    DARK_BLUE("dark_blue", NamedTextColor.DARK_BLUE),
    DARK_GREEN("dark_green", NamedTextColor.DARK_GREEN),
    DARK_AQUA("dark_aqua", NamedTextColor.DARK_AQUA),
    DARK_RED("dark_red", NamedTextColor.DARK_RED),
    DARK_PURPLE("dark_purple", NamedTextColor.DARK_PURPLE),
    GOLD("gold", NamedTextColor.GOLD),
    GRAY("gray", NamedTextColor.GRAY),
    DARK_GRAY("dark_gray", NamedTextColor.DARK_GRAY),
    BLUE("blue", NamedTextColor.BLUE),
    GREEN("green", NamedTextColor.GREEN),
    AQUA("aqua", NamedTextColor.AQUA),
    RED("red", NamedTextColor.RED),
    LIGHT_PURPLE("light_purple", NamedTextColor.LIGHT_PURPLE),
    YELLOW("yellow", NamedTextColor.YELLOW),
    WHITE("white", NamedTextColor.WHITE);

    public static final NetworkBuffer.Type<TeamColor> NETWORK_TYPE = NetworkBuffer.Enum(TeamColor.class);
    public static final Codec<TeamColor> CODEC = Codec.Enum(TeamColor.class);

    public static @Nullable TeamColor fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private final String name;
    private final NamedTextColor textColor;

    TeamColor(String name, NamedTextColor textColor) {
        this.name = name;
        this.textColor = textColor;
    }

    public TextColor textColor() {
        return this.textColor;
    }

    @Override
    public @Range(from = 0L, to = 255L) int red() {
        return this.textColor.red();
    }

    @Override
    public @Range(from = 0L, to = 255L) int green() {
        return this.textColor.green();
    }

    @Override
    public @Range(from = 0L, to = 255L) int blue() {
        return this.textColor.blue();
    }

    public HSVLike asHSV() {
        return textColor.asHSV();
    }

    @Override
    public String toString() {
        return this.name;
    }

}
