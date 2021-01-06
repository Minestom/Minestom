package net.minestom.server.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a {@link net.minestom.server.entity.LivingEntity living entity} attribute.
 */
public class Attribute {

    private static final Map<String, Attribute> ATTRIBUTES = new HashMap<>();

    static {
        Attributes.init();
    }

    private final String key;
    private final float defaultValue;
    private final float maxValue;
    private final boolean shareWithClient;

    /**
     * Create a new attribute with a given key and default.
     * <p>
     * By default, this attribute will be sent to the client.
     * </p>
     *
     * @param key          the attribute registry key
     * @param defaultValue the default value
     * @param maxValue     the maximum allowed value
     */
    public Attribute(@NotNull String key, float defaultValue, float maxValue) {
        this(key, true, defaultValue, maxValue);
    }

    /**
     * Create a new attribute with a given key and default.
     *
     * @param key             the attribute registry key
     * @param shareWithClient whether to send this attribute to the client
     * @param defaultValue    the default value
     * @param maxValue        the maximum allowed value
     */
    public Attribute(@NotNull String key, boolean shareWithClient, float defaultValue, float maxValue) {
        if (defaultValue > maxValue) {
            throw new IllegalArgumentException("Default value cannot be greater than the maximum allowed");
        }
        this.key = key;
        this.shareWithClient = shareWithClient;
        this.defaultValue = defaultValue;
        this.maxValue = maxValue;
    }

    /**
     * Gets the attribute unique key.
     *
     * @return the attribute key
     */
    @NotNull
    public String getKey() {
        return key;
    }

    /**
     * Gets the attribute default value that should be applied.
     *
     * @return the attribute default value
     */
    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the maximum value applicable to an entity for this attribute.
     *
     * @return the maximum value of this attribute
     */
    public float getMaxValue() {
        return maxValue;
    }

    /**
     * Gets whether this attribute's instances should be sent to clients.
     *
     * @return if this attribute is to be shared
     */
    public boolean isShared() {
        return shareWithClient;
    }

    /**
     * Register this attribute.
     *
     * @return this attribute
     * @see #fromKey(String)
     * @see #values()
     */
    @NotNull
    public Attribute register() {
        ATTRIBUTES.put(key, this);
        return this;
    }

    /**
     * Retrieves an attribute by its key.
     *
     * @param key the key of the attribute
     * @return the attribute for the key or null if not any
     */
    @Nullable
    public static Attribute fromKey(@NotNull String key) {
        return ATTRIBUTES.get(key);
    }

    /**
     * Retrieves all registered attributes.
     *
     * @return an array containing all registered attributes
     */
    @NotNull
    public static Attribute[] values() {
        return ATTRIBUTES.values().toArray(new Attribute[0]);
    }

    /**
     * Retrieves registered attributes that are shared with the client.
     *
     * @return an array containing registered, sharable attributes
     */
    @NotNull
    public static Attribute[] sharedAttributes() {
        return ATTRIBUTES.values().stream().filter(Attribute::isShared).toArray(Attribute[]::new);
    }
}
