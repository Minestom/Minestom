package net.minestom.server.entity.attribute;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeInstanceTest {

    @Test
    void testReplaceAttributeSameValue() {
        var attribute = new AttributeInstance(Attribute.SAFE_FALL_DISTANCE, null);
        var modifier = new AttributeModifier("test", 1.0, AttributeOperation.ADD_VALUE);

        attribute.addModifier(modifier);
        assertEquals(4, attribute.getValue());

        attribute.addModifier(modifier);
        assertEquals(4, attribute.getValue());
    }

    @Test
    void testReplaceAttributeNewValue() {
        var attribute = new AttributeInstance(Attribute.SAFE_FALL_DISTANCE, null);

        attribute.addModifier(new AttributeModifier("test", 1.0, AttributeOperation.ADD_VALUE));
        assertEquals(4, attribute.getValue());

        attribute.addModifier(new AttributeModifier("test", 2.0, AttributeOperation.ADD_VALUE));
        assertEquals(5, attribute.getValue()); // New value

        attribute.addModifier(new AttributeModifier("test", 2.0, AttributeOperation.MULTIPLY_BASE));
        assertEquals(9, attribute.getValue()); // New operation

    }

}
