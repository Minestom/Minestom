package net.minestom.server.entity.attribute;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Represents an instance of an attribute and its modifiers. This class is thread-safe (you do not need to acquire the
 * entity to modify its attributes).
 */
public final class AttributeInstance {
    public static final NetworkBuffer.Type<AttributeInstance> NETWORK_TYPE = NetworkBufferTemplate.template(
            Attribute.NETWORK_TYPE, AttributeInstance::attribute,
            NetworkBuffer.DOUBLE, AttributeInstance::getBaseValue,
            AttributeModifier.NETWORK_TYPE.list(Short.MAX_VALUE), value -> List.copyOf(value.modifiers()),
            (attribute, baseValue, modifiers) -> new AttributeInstance(attribute, baseValue, modifiers, null)
    );

    private final Attribute attribute;
    private final Map<Key, AttributeModifier> modifiers;
    private final Collection<AttributeModifier> unmodifiableModifiers;
    private final AtomicLong baseValueBits;

    private final Consumer<AttributeInstance> propertyChangeListener;
    private volatile double cachedValue = 0.0D;

    public AttributeInstance(Attribute attribute, @Nullable Consumer<AttributeInstance> listener) {
        this(attribute, attribute.defaultValue(), new ArrayList<>(), listener);
    }

    public AttributeInstance(Attribute attribute, double baseValue, Collection<AttributeModifier> modifiers, @Nullable Consumer<AttributeInstance> listener) {
        this.attribute = attribute;
        this.modifiers = new ConcurrentHashMap<>();
        for (var modifier : modifiers) this.modifiers.put(modifier.id(), modifier);
        this.unmodifiableModifiers = Collections.unmodifiableCollection(this.modifiers.values());
        this.baseValueBits = new AtomicLong(Double.doubleToLongBits(baseValue));

        this.propertyChangeListener = listener;
        refreshCachedValue(baseValue);
    }

    /**
     * Gets the attribute associated to this instance.
     *
     * @return the associated attribute
     */
    public Attribute attribute() {
        return attribute;
    }

    /**
     * The base value of this instance without modifiers
     *
     * @return the instance base value
     * @see #setBaseValue(double)
     */
    public double getBaseValue() {
        return Double.longBitsToDouble(baseValueBits.get());
    }

    /**
     * Sets the base value of this instance.
     *
     * @param baseValue the new base value
     * @see #getBaseValue()
     */
    public void setBaseValue(double baseValue) {
        long newBits = Double.doubleToLongBits(baseValue);
        long oldBits = this.baseValueBits.getAndSet(newBits);
        if (oldBits != newBits) {
            refreshCachedValue(baseValue);
        }
    }

    /**
     * Get the modifiers applied to this instance.
     *
     * @return an immutable collection of the modifiers applied to this attribute.
     */
    @UnmodifiableView
    public Collection<AttributeModifier> modifiers() {
        return unmodifiableModifiers;
    }

    /**
     * Add a modifier to this instance.
     *
     * @param modifier the modifier to add
     * @return the old modifier, or null if none
     */
    public @Nullable AttributeModifier addModifier(AttributeModifier modifier) {
        final AttributeModifier previousModifier = modifiers.put(modifier.id(), modifier);
        if (!modifier.equals(previousModifier)) refreshCachedValue(getBaseValue());
        return previousModifier;
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier the modifier to remove
     * @return the modifier that was removed, or null if none
     */
    public @Nullable AttributeModifier removeModifier(AttributeModifier modifier) {
        return removeModifier(modifier.id());
    }

    /**
     * Clears all modifiers on this instance, excepting those whose ID is defined in
     * {@link LivingEntity#PROTECTED_MODIFIERS}.
     */
    public void clearModifiers() {
        this.modifiers.values().removeIf(modifier -> !LivingEntity.PROTECTED_MODIFIERS.contains(modifier.id()));
        refreshCachedValue(getBaseValue());
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param id The namespace id of the modifier to remove
     * @return the modifier that was removed, or null if none
     */
    public @Nullable AttributeModifier removeModifier(Key id) {
        final AttributeModifier removed = modifiers.remove(id);
        if (removed != null) {
            refreshCachedValue(getBaseValue());
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
    public double applyModifiers(double baseValue) {
        return computeValue(baseValue);
    }

    private double computeValue(double base) {
        final Collection<AttributeModifier> modifiers = modifiers();

        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.ADD_VALUE).toArray(AttributeModifier[]::new)) {
            base += modifier.amount();
        }

        double result = base;

        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.ADD_MULTIPLIED_BASE).toArray(AttributeModifier[]::new)) {
            result += (base * modifier.amount());
        }
        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.ADD_MULTIPLIED_TOTAL).toArray(AttributeModifier[]::new)) {
            result *= (1.0f + modifier.amount());
        }

        return Math.clamp(result, getAttribute().minValue(), getAttribute().maxValue());
    }

    /**
     * Recalculate the value of this attribute instance using the modifiers.
     */
    private void refreshCachedValue(double baseValue) {
        this.cachedValue = computeValue(baseValue);

        // Signal entity
        if (propertyChangeListener != null) {
            propertyChangeListener.accept(this);
        }
    }

    @Deprecated
    public Collection<AttributeModifier> getModifiers() {
        return modifiers();
    }

    @Deprecated
    public Attribute getAttribute() {
        return attribute;
    }
}
