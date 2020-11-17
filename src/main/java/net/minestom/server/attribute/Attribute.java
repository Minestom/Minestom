package net.minestom.server.attribute;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Attribute {

	private static final Map<String, Attribute> ATTRIBUTES = new HashMap<>();

	private final String key;
	private final float defaultValue;
	private final float maxValue;
	private final boolean shareWithClient;

	public Attribute(@NotNull String key, float defaultValue, float maxValue) {
		this(key, false, defaultValue, maxValue);
	}

	public Attribute(@NotNull String key, boolean shareWithClient, float defaultValue, float maxValue) {
		if (defaultValue > maxValue) {
			throw new IllegalArgumentException("Default value cannot be greater than the maximum allowed");
		}
		this.key = key;
		this.shareWithClient = shareWithClient;
		this.defaultValue = defaultValue;
		this.maxValue = maxValue;
	}

	@NotNull
	public String getKey() {
		return key;
	}

	public float getDefaultValue() {
		return defaultValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	public boolean isShared() {
		return shareWithClient;
	}

	@NotNull
	public Attribute register() {
		ATTRIBUTES.put(key, this);
		return this;
	}

	@Nullable
	public static Attribute fromKey(@NotNull String key) {
		return ATTRIBUTES.get(key);
	}

	@NotNull
	public static Attribute[] values() {
		return ATTRIBUTES.values().toArray(new Attribute[0]);
	}

	@NotNull
	public static Attribute[] sharedAttributes() {
		return ATTRIBUTES.values().stream().filter(Attribute::isShared).toArray(Attribute[]::new);
	}
}
