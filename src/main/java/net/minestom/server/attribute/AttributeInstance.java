package net.minestom.server.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@NotNull
	public Attribute getAttribute() {
		return attribute;
	}

	public float getBaseValue() {
		return baseValue;
	}

	private void setDirty() {
		if (!dirty) {
			dirty = true;
			if (propertyChangeListener != null) {
				propertyChangeListener.accept(this);
			}
		}
	}

	public void setBaseValue(float baseValue) {
		if (this.baseValue != baseValue) {
			this.baseValue = baseValue;
			setDirty();
		}
	}

	public void addModifier(@NotNull AttributeModifier modifier) {
		if (modifiers.putIfAbsent(modifier.getId(), modifier) == null) {
			setDirty();
		}
	}

	public void removeModifier(@NotNull AttributeModifier modifier) {
		if (modifiers.remove(modifier.getId()) != null) {
			setDirty();
		}
	}

	public Collection<AttributeModifier> getModifiers() {
		return modifiers.values();
	}

	public float getValue() {
		if (dirty) {
			cachedValue = processModifiers();
			dirty = false;
		}
		return cachedValue;
	}

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
