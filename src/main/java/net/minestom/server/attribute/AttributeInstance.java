package net.minestom.server.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an instance of an attribute and its modifiers.
 */
public class AttributeInstance {

	private final Attribute attribute;
	private final Map<UUID, AttributeModifier> modifiers = new HashMap<>();
	private final Consumer<AttributeInstance> propertyChangeListener;
	private float baseValue;
	private boolean dirty = true;
	private float cachedValue = 0.0f;

	public AttributeInstance(@NotNull Attribute attribute, @Nullable Consumer<AttributeInstance> listener) {
		this.attribute = attribute;
		this.propertyChangeListener = listener;
		this.baseValue = attribute.getDefaultValue();
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
	 * @see #setBaseValue(float)
	 * 
	 * @return the instance base value
	 */
	public float getBaseValue() {
		return baseValue;
	}

	/**
	 * Sets this instance dirty to trigger calculation of the new value.
	 * Triggers the {@link #propertyChangeListener}.
	 */
	private void setDirty() {
		if (!dirty) {
			dirty = true;
			if (propertyChangeListener != null) {
				propertyChangeListener.accept(this);
			}
		}
	}

	/**
	 * Sets the base value of this instance.
	 * 
	 * @see #getBaseValue() 
	 * 
	 * @param baseValue the new base value
	 */
	public void setBaseValue(float baseValue) {
		if (this.baseValue != baseValue) {
			this.baseValue = baseValue;
			setDirty();
		}
	}

	/**
	 * Add a modifier to this instance.
	 *
	 * @param modifier the modifier to add
	 */
	public void addModifier(@NotNull AttributeModifier modifier) {
		if (modifiers.putIfAbsent(modifier.getId(), modifier) == null) {
			setDirty();
		}
	}

	/**
	 * Remove a modifier from this instance.
	 *
	 * @param modifier the modifier to remove
	 */
	public void removeModifier(@NotNull AttributeModifier modifier) {
		if (modifiers.remove(modifier.getId()) != null) {
			setDirty();
		}
	}

	/**
	 * Get the modifiers applied to this instance.
	 *
	 * @return the modifiers.
	 */
	public Collection<AttributeModifier> getModifiers() {
		return modifiers.values();
	}

	/**
	 * Gets the value of this instance calculated with modifiers applied.
	 *
	 * @return the attribute value
	 */
	public float getValue() {
		if (dirty) {
			cachedValue = processModifiers();
			dirty = false;
		}
		return cachedValue;
	}

	/**
	 * Recalculate the value of this attribute instance using the modifiers.
	 *
	 * @return the attribute value
	 */
	protected float processModifiers() {
		float base = getBaseValue();

		for (var modifier : modifiers.values().stream().filter(mod -> mod.getOperation() == AttributeOperation.ADDITION).toArray(AttributeModifier[]::new)) {
			base += modifier.getAmount();
		}

		float result = base;

		for (var modifier : modifiers.values().stream().filter(mod -> mod.getOperation() == AttributeOperation.MULTIPLY_BASE).toArray(AttributeModifier[]::new)) {
			result += (base * modifier.getAmount());
		}
		for (var modifier : modifiers.values().stream().filter(mod -> mod.getOperation() == AttributeOperation.MULTIPLY_TOTAL).toArray(AttributeModifier[]::new)) {
			result *= (1.0f + modifier.getAmount());
		}

		return Math.min(result, getAttribute().getMaxValue());
	}
}
