package net.minestom.server.item.attribute;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;

import java.util.UUID;

public class ItemAttribute {

    private UUID uuid;
    private String internalName;
    private Attribute attribute;
    private AttributeOperation operation;
    private double value;
    private AttributeSlot slot;

    public ItemAttribute(UUID uuid, String internalName, Attribute attribute, AttributeOperation operation, double value, AttributeSlot slot) {
        this.uuid = uuid;
        this.internalName = internalName;
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
        this.slot = slot;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getInternalName() {
        return internalName;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    public AttributeSlot getSlot() {
        return slot;
    }
}
