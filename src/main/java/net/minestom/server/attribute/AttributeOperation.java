package net.minestom.server.attribute;

public enum AttributeOperation {
    ADDITION(0),
    MULTIPLY_BASE(1),
    MULTIPLY_TOTAL(2);

    private static final AttributeOperation[] VALUES = new AttributeOperation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
    private final int id;

    AttributeOperation(int id) {
        this.id = id;
    }

    public static AttributeOperation byId(int id) {
        if (id >= 0 && id < VALUES.length) {
            return VALUES[id];
        } else {
            throw new IllegalArgumentException("No operation with value " + id);
        }
    }

    public int getId() {
        return this.id;
    }
}
