package net.minestom.server.attribute;

/**
 * Represents a {@link net.minestom.server.entity.LivingEntity living entity} attribute.
 */
public record Attribute(String key, float defaultValue, float maxValue) {

    public Attribute {
        if (defaultValue > maxValue) {
            throw new IllegalArgumentException("Default value cannot be greater than the maximum allowed");
        }
    }
}
