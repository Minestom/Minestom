package net.minestom.server.bossbar;

/**
 * Used to define the number of segments on a {@link BossBar}.
 *
 * @deprecated Use {@link net.kyori.adventure.bossbar.BossBar.Overlay}
 */
@Deprecated
public enum BarDivision {
    SOLID,
    SEGMENT_6,
    SEGMENT_10,
    SEGMENT_12,
    SEGMENT_20;

    public net.kyori.adventure.bossbar.BossBar.Overlay asAdventureOverlay() {
        return net.kyori.adventure.bossbar.BossBar.Overlay.values()[this.ordinal()];
    }
}
