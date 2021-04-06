package net.minestom.server.bossbar;

/**
 * Represents the displayed color of a {@link BossBar}.
 * @deprecated Use {@link net.kyori.adventure.bossbar.BossBar.Color}
 */
@Deprecated
public enum BarColor {
    PINK,
    BLUE,
    RED,
    GREEN,
    YELLOW,
    PURPLE,
    WHITE;

    public net.kyori.adventure.bossbar.BossBar.Color asAdventureColor() {
        return net.kyori.adventure.bossbar.BossBar.Color.valueOf(this.name());
    }
}
