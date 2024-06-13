package net.minestom.server.entity.attribute;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents an instance of an attribute and its modifiers.
 */
public final class AttributeInstance {
    public static final NetworkBuffer.Type<AttributeInstance> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, AttributeInstance value) {
            buffer.write(Attribute.NETWORK_TYPE, value.attribute());
            buffer.write(NetworkBuffer.DOUBLE, value.getBaseValue());
            buffer.writeCollection(AttributeModifier.NETWORK_TYPE, value.modifiers());
        }

        @Override
        public AttributeInstance read(@NotNull NetworkBuffer buffer) {
            return new AttributeInstance(buffer.read(Attribute.NETWORK_TYPE), buffer.read(NetworkBuffer.DOUBLE),
                    buffer.readCollection(AttributeModifier.NETWORK_TYPE, Short.MAX_VALUE), null);
        }
    };

    private final Attribute attribute;
    private final Map<NamespaceID, AttributeModifier> modifiers;
    private final Collection<AttributeModifier> unmodifiableModifiers;
    private double baseValue;

    private final Consumer<AttributeInstance> propertyChangeListener;
    private double cachedValue = 0.0f;

    public AttributeInstance(@NotNull Attribute attribute, @Nullable Consumer<AttributeInstance> listener) {
        this(attribute, attribute.defaultValue(), new ArrayList<>(), listener);
    }

    public AttributeInstance(@NotNull Attribute attribute, double baseValue, @NotNull Collection<AttributeModifier> modifiers, @Nullable Consumer<AttributeInstance> listener) {
        this.attribute = attribute;
        this.modifiers = new HashMap<>();
        for (var modifier : modifiers) this.modifiers.put(modifier.id(), modifier);
        this.unmodifiableModifiers = Collections.unmodifiableCollection(this.modifiers.values());
        this.baseValue = baseValue;

        this.propertyChangeListener = listener;
        refreshCachedValue();
    }

    /**
     * Gets the attribute associated to this instance.
     *
     * @return the associated attribute
     */
    public @NotNull Attribute attribute() {
        return attribute;
    }

    /**
     * The base value of this instance without modifiers
     *
     * @return the instance base value
     * @see #setBaseValue(double)
     */
    public double getBaseValue() {
        return baseValue;
    }

    /**
     * Sets the base value of this instance.
     *
     * @param baseValue the new base value
     * @see #getBaseValue()
     */
    public void setBaseValue(double baseValue) {
        if (this.baseValue != baseValue) {
            this.baseValue = baseValue;
            refreshCachedValue();
        }
    }

    /**
     * Get the modifiers applied to this instance.
     *
     * @return an immutable collection of the modifiers applied to this attribute.
     */
    @NotNull
    public Collection<AttributeModifier> modifiers() {
        return unmodifiableModifiers;
    }

    /**
     * Add a modifier to this instance.
     *
     * @param modifier the modifier to add
     */
    public void addModifier(@NotNull AttributeModifier modifier) {
        if (modifiers.putIfAbsent(modifier.id(), modifier) == null) {
            refreshCachedValue();
        }
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier the modifier to remove
     */
    public void removeModifier(@NotNull AttributeModifier modifier) {
        removeModifier(modifier.id());
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param id The namespace id of the modifier to remove
     */
    public void removeModifier(@NotNull NamespaceID id) {
        if (modifiers.remove(id) != null) {
            refreshCachedValue();
        }
    }

    /**
     * Gets the value of this instance calculated with modifiers applied.
     *
     * @return the attribute value
     */
    public double getValue() {
        return cachedValue;
    }

    /**
     * Recalculate the value of this attribute instance using the modifiers.
     */
    private void refreshCachedValue() {
        final Collection<AttributeModifier> modifiers = getModifiers();
        double base = getBaseValue();

        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.ADD_VALUE).toArray(AttributeModifier[]::new)) {
            base += modifier.amount();
        }

        double result = base;

        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.MULTIPLY_BASE).toArray(AttributeModifier[]::new)) {
            result += (base * modifier.amount());
        }
        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.MULTIPLY_TOTAL).toArray(AttributeModifier[]::new)) {
            result *= (1.0f + modifier.amount());
        }

        this.cachedValue = Math.clamp(result, getAttribute().minValue(), getAttribute().maxValue());

        // Signal entity
        if (propertyChangeListener != null) {
            propertyChangeListener.accept(this);
        }
    }

    @Deprecated
    @NotNull
    public Collection<AttributeModifier> getModifiers() {
        return modifiers();
    }

    @Deprecated
    public @NotNull Attribute getAttribute() {
        return attribute;
    }
}
