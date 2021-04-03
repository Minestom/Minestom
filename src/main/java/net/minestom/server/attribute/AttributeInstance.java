package net.minestom.server.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents an instance of an attribute and its modifiers.
 */
public class AttributeInstance {

    private final Attribute attribute;
    private final Map<UUID, AttributeModifier> modifiers = new HashMap<>();
    private final Consumer<AttributeInstance> propertyChangeListener;
    private float baseValue;
    private float cachedValue = 0.0f;

    public AttributeInstance(@NotNull Attribute attribute, @Nullable Consumer<AttributeInstance> listener) {
        this.attribute = attribute;
        this.propertyChangeListener = listener;
        this.baseValue = attribute.getDefaultValue();
        refreshCachedValue();
    }

    /**
     * Gets the attribute associated to this instance.
     *
     * @return the associated attribute
     */
    @NotNull
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * The base value of this instance without modifiers
     *
     * @return the instance base value
     * @see #setBaseValue(float)
     */
    public float getBaseValue() {
        return baseValue;
    }

    /**
     * Sets the base value of this instance.
     *
     * @param baseValue the new base value
     * @see #getBaseValue()
     */
    public void setBaseValue(float baseValue) {
        if (this.baseValue != baseValue) {
            this.baseValue = baseValue;
            refreshCachedValue();
        }
    }

    /**
     * Add a modifier to this instance.
     *
     * @param modifier the modifier to add
     */
    public void addModifier(@NotNull AttributeModifier modifier) {
        if (modifiers.putIfAbsent(modifier.getId(), modifier) == null) {
            refreshCachedValue();
        }
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier the modifier to remove
     */
    public void removeModifier(@NotNull AttributeModifier modifier) {
        if (modifiers.remove(modifier.getId()) != null) {
            refreshCachedValue();
        }
    }

    /**
     * Get the modifiers applied to this instance.
     *
     * @return the modifiers.
     */
    @NotNull
    public Collection<AttributeModifier> getModifiers() {
        return modifiers.values();
    }

    /**
     * Gets the value of this instance calculated with modifiers applied.
     *
     * @return the attribute value
     */
    public float getValue() {
        return cachedValue;
    }

    /**
     * Recalculate the value of this attribute instance using the modifiers.
     */
    protected void refreshCachedValue() {
        final Collection<AttributeModifier> modifiers = getModifiers();
        float base = getBaseValue();

        for (var modifier : modifiers.stream().filter(mod -> mod.getOperation() == AttributeOperation.ADDITION).toArray(AttributeModifier[]::new)) {
            base += modifier.getAmount();
        }

        float result = base;

        for (var modifier : modifiers.stream().filter(mod -> mod.getOperation() == AttributeOperation.MULTIPLY_BASE).toArray(AttributeModifier[]::new)) {
            result += (base * modifier.getAmount());
        }
        for (var modifier : modifiers.stream().filter(mod -> mod.getOperation() == AttributeOperation.MULTIPLY_TOTAL).toArray(AttributeModifier[]::new)) {
            result *= (1.0f + modifier.getAmount());
        }

        this.cachedValue = Math.min(result, getAttribute().getMaxValue());

        // Signal entity
        if (propertyChangeListener != null) {
            propertyChangeListener.accept(this);
        }
    }
}
