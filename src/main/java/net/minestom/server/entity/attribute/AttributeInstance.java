package net.minestom.server.entity.attribute;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
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
    public static final NetworkBuffer.Type<AttributeInstance> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, AttributeInstance value) {
            buffer.write(Attribute.NETWORK_TYPE, value.attributeKey());
            buffer.write(NetworkBuffer.DOUBLE, value.getBaseValue());
            buffer.writeCollection(AttributeModifier.NETWORK_TYPE, value.modifiers());
        }

        @Override
        public AttributeInstance read(@NotNull NetworkBuffer buffer) {
            return new AttributeInstance(buffer.read(Attribute.NETWORK_TYPE), buffer.read(NetworkBuffer.DOUBLE),
                    buffer.readCollection(AttributeModifier.NETWORK_TYPE, Short.MAX_VALUE), null);
        }
    };

    private final DynamicRegistry.Key<Attribute> attributeKey;
    private final Attribute attribute;

    private final Map<NamespaceID, AttributeModifier> modifiers;
    private final Collection<AttributeModifier> unmodifiableModifiers;
    private final AtomicLong baseValueBits;

    private final Consumer<AttributeInstance> propertyChangeListener;
    private volatile double cachedValue = 0.0D;

    public AttributeInstance(@NotNull DynamicRegistry.Key<Attribute> attributeKey, @Nullable Consumer<AttributeInstance> listener) {
        this.attributeKey = attributeKey;
        this.attribute = Attribute.lookup(attributeKey, MinecraftServer.process());

        this.modifiers = new ConcurrentHashMap<>();
        this.unmodifiableModifiers = Collections.unmodifiableCollection(this.modifiers.values());
        this.baseValueBits = new AtomicLong(Double.doubleToLongBits(attribute.defaultValue()));

        this.propertyChangeListener = listener;
        refreshCachedValue(attribute.defaultValue());
    }

    public AttributeInstance(@NotNull DynamicRegistry.Key<Attribute> attributeKey, double baseValue,
                             @NotNull Collection<AttributeModifier> modifiers, @Nullable Consumer<AttributeInstance> listener) {
        this.attributeKey = attributeKey;
        this.attribute = Attribute.lookup(attributeKey, MinecraftServer.process());

        this.modifiers = new ConcurrentHashMap<>();
        for (var modifier : modifiers) this.modifiers.put(modifier.id(), modifier);
        this.unmodifiableModifiers = Collections.unmodifiableCollection(this.modifiers.values());
        this.baseValueBits = new AtomicLong(Double.doubleToLongBits(baseValue));

        this.propertyChangeListener = listener;
        refreshCachedValue(baseValue);
    }

    /**
     * Gets the actual {@link Attribute} instance used by this instance.
     * @return the actual attribute that was looked up in the registry
     */
    public @NotNull Attribute attribute() {
        return attribute;
    }

    /**
     * Gets the key for attribute associated to this instance.
     *
     * @return the associated attribute
     */
    public @NotNull DynamicRegistry.Key<Attribute> attributeKey() {
        return attributeKey;
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
    @NotNull
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
    public AttributeModifier addModifier(@NotNull AttributeModifier modifier) {
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
    public AttributeModifier removeModifier(@NotNull AttributeModifier modifier) {
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
    public AttributeModifier removeModifier(@NotNull NamespaceID id) {
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

        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.MULTIPLY_BASE).toArray(AttributeModifier[]::new)) {
            result += (base * modifier.amount());
        }
        for (var modifier : modifiers.stream().filter(mod -> mod.operation() == AttributeOperation.MULTIPLY_TOTAL).toArray(AttributeModifier[]::new)) {
            result *= (1.0f + modifier.amount());
        }

        return Math.clamp(result, attribute.minValue(), attribute.maxValue());
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
    @NotNull
    public Collection<AttributeModifier> getModifiers() {
        return modifiers();
    }

    @Deprecated
    public @NotNull DynamicRegistry.Key<Attribute> getAttribute() {
        return attributeKey;
    }
}
