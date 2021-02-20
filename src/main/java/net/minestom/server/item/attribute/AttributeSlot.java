package net.minestom.server.item.attribute;

public enum AttributeSlot {
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    public static AttributeSlot parse(String string) {
        switch (string.toUpperCase()) {
            case "OFFHAND":
                return OFFHAND;
            case "FEET":
                return FEET;
            case "LEGS":
                return LEGS;
            case "CHEST":
                return CHEST;
            case "HEAD":
                return HEAD;
            default:
                return MAINHAND;
        }
    }
}
