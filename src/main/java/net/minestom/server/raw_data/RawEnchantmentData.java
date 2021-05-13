package net.minestom.server.raw_data;

public final class RawEnchantmentData {
    private final int maxLevel;
    private final int minLevel;
    private final String rarity; // TODO: Dedicated object?
    private final boolean curse;
    private final boolean discoverable;
    private final boolean tradeable;
    private final boolean treasureExclusive;
    private final String category; // TODO: Dedicated object?

    public RawEnchantmentData(int maxLevel, int minLevel, String rarity, boolean curse, boolean discoverable, boolean tradeable, boolean treasureExclusive, String category) {
        this.maxLevel = maxLevel;
        this.minLevel = minLevel;
        this.rarity = rarity;
        this.curse = curse;
        this.discoverable = discoverable;
        this.tradeable = tradeable;
        this.treasureExclusive = treasureExclusive;
        this.category = category;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public String getRarity() {
        return rarity;
    }

    public boolean isCurse() {
        return curse;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public boolean isTradeable() {
        return tradeable;
    }

    public boolean isTreasureExclusive() {
        return treasureExclusive;
    }

    public String getCategory() {
        return category;
    }
}