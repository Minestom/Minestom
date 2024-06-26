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
     * @return the old modifier, or null if none
     */
    public AttributeModifier addModifier(@NotNull AttributeModifier modifier) {
        final AttributeModifier old = modifiers.putIfAbsent(modifier.id(), modifier);
        if (old == null) {
            refreshCachedValue();
        }

        return old;
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier the modifier to remove
     * @return the modifier that was removed, or null if none
     */
    public AttributeModifier removeModifier(@NotNull AttributeModifier modifier) {
        return removeModifier(modifier.id());
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param id The namespace id of the modifier to remove
     * @return the modifier that was removed, or null if none
     */
    public AttributeModifier removeModifier(@NotNull NamespaceID id) {
        final AttributeModifier removed = modifiers.remove(id);
        if (removed != null) {
            refreshCachedValue();
        }

        return removed;
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
     * Gets the value of this instance, calculated assuming the given {@code baseValue}.
     *
     * @param baseValue the value to be used as the base for this operation, rather than this instance's normal base
     *                  value
     * @return the attribute value
     */
    public double getValueWithBase(double baseValue) {
        return computeValue(baseValue);
    }

    private double computeValue(double base) {
        final Collection<AttributeModifier> modifiers = getModifiers();

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

        return Math.clamp(result, getAttribute().minValue(), getAttribute().maxValue());
    }

    /**
     * Recalculate the value of this attribute instance using the modifiers.
     */
    private void refreshCachedValue() {
        this.cachedValue = computeValue(getBaseValue());

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
