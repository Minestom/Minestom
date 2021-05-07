package net.minestom.server.weather;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Objects;

/**
 * Weather.
 */
@NonExtendable
public interface Weather {
    /**
     * Creates some clear weather that lasts forever.
     *
     * @return the weather
     */
    static @NotNull Weather clear() {
        return infinite(Type.CLEAR);
    }

    /**
     * Creates some clear weather with a given length.
     *
     * @param length the length
     * @return the weather
     */
    static @NotNull Weather clear(@NotNull Duration length) {
        return of(Type.CLEAR, length);
    }

    /**
     * Creates some rain that lasts forever.
     *
     * @return the weather
     */
    static @NotNull Weather rain() {
        return infinite(Type.RAIN);
    }

    /**
     * Creates some rain with a given length.
     *
     * @param length the length
     * @return the weather
     */
    static @NotNull Weather rain(@NotNull Duration length) {
        return of(Type.RAIN, length);
    }

    /**
     * Creates some thunder that lasts forever.
     *
     * @return the weather
     */
    static @NotNull Weather thunder() {
        return infinite(Type.THUNDER);
    }

    /**
     * Creates some thunder with a given length.
     *
     * @param length the length
     * @return the weather
     */
    static @NotNull Weather thunder(@NotNull Duration length) {
        return of(Type.THUNDER, length);
    }

    /**
     * Creates some weather of an infinite length.
     *
     * @param type the type
     * @return the weather
     */
    static @NotNull Weather infinite(@NotNull Type type) {
        switch (Objects.requireNonNull(type, "type")) {
            case CLEAR: return WeatherImpl.INFINITELY_CLEAR;
            case RAIN: return WeatherImpl.INFINITELY_RAINING;
            case THUNDER: return WeatherImpl.INFINITELY_THUNDERING;
            default: throw new IllegalArgumentException("unknown weather type");
        }
    }

    /**
     * Creates some weather from a type and duration.
     *
     * @param type the type
     * @param length the duration
     * @return the weather
     */
    static @NotNull Weather of(@NotNull Type type, @NotNull Duration length) {
        return new WeatherImpl(type, length);
    }

    /**
     * Gets a new weather builder.
     *
     * @return the builder
     */
    static @NotNull Builder builder() {
        return new WeatherImpl.BuilderImpl();
    }

    /**
     * Gets the type of this weather.
     *
     * @return the type
     */
    @NotNull Type getType();

    /**
     * Gets the amount of time that this weather has left.
     *
     * @return the duration, or {@code null} if this weather is not due to expire
     */
    @Nullable Duration getRemainingDuration();

    /**
     * Gets the instant that this weather will expire, if any.
     *
     * @return the instant it will expire, or {@code null} if this weather is not due to expire
     */
    @Nullable Instant getExpiration();

    /**
     * Checks if this weather has expired.
     *
     * @return [@code true} if it has expired
     */
    boolean hasExpired();

    /**
     * Gets the rain strength.
     *
     * @return the rain strength
     * @see Builder#rainStrength(float)
     */
    float getRainStrength();

    /**
     * Gets the thunder strength.
     *
     * @return the thunder strength
     * @see Builder#thunderStrength(float)
     */
    float getThunderStrength();

    /**
     * Creates a builder from this weather instance.
     *
     * @return the builder
     */
    @Contract(value = "-> new", pure = true)
    @NotNull Builder toBuilder();

    /**
     * Checks if this weather is equal to another weather, ignoring the expiration.
     *
     * @param that the other weather
     * @return if the weathers are equal, ignoring the expiration
     */
    boolean equalsIgnoreExpiration(@NotNull Weather that);

    /**
     * The types of weather.
     */
    enum Type {
        /**
         * Clear skies.
         */
        CLEAR,

        /**
         * Rain.
         */
        RAIN,

        /**
         * Thunder.
         */
        THUNDER
    }

    /**
     * A builder for weather.
     */
    @NonExtendable
    interface Builder {
        /**
         * Sets the weather type.
         *
         * @param type the type
         * @return {@code this}, for chaining
         */
        @Contract("_ -> this")
        @NotNull Builder type(@NotNull Type type);

        /**
         * Sets how long the weather will last.
         *
         * @param milliseconds the length in milliseconds
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code milliseconds} is negative
         */
        default @NotNull Builder length(long milliseconds) {
            if (milliseconds < 0L) throw new IllegalArgumentException("milliseconds cannot be negative");
            return this.length(Duration.ofMillis(milliseconds));
        }

        /**
         * Sets how long the weather will last.
         *
         * @param length the length
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code length} is negative
         */
        @Contract("_ -> this")
        default @NotNull Builder length(@NotNull Period length) {
            Objects.requireNonNull(length, "length");
            if (length.isNegative()) throw new IllegalArgumentException("length cannot be negative");
            return this.expiration(Instant.now().plus(length));
        }

        /**
         * Sets how long the weather will last.
         *
         * @param length the length
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code length} is negative
         */
        @Contract("_ -> this")
        default @NotNull Builder length(@NotNull Duration length) {
            Objects.requireNonNull(length, "length");
            if (length.isNegative()) throw new IllegalArgumentException("length cannot be negative");
            return this.expiration(Instant.now().plus(length));
        }

        /**
         * Sets the instant that the weather will expire.
         *
         * @param expiration the expiration instant
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code expiration} is in the past
         */
        @Contract("_ -> this")
        @NotNull Builder expiration(@NotNull Instant expiration);

        /**
         * Sets the weather to last forever.
         *
         * @return {@code this}, for chaining
         */
        @Contract("-> this")
        @NotNull Builder infinite();

        /**
         * Sets the strength of the rain. This value must be equal to or greater than
         * zero. If the strength is zero, the rain will not be displayed. A value of one
         * is the highest value sent in vanilla, but higher values are possible.
         *
         * <p>This value is ignored if the type is {@link Type#CLEAR}.</p>
         *
         * @param strength the strength
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code strength} is less than zero
         */
        @Contract("_ -> this")
        @NotNull Builder rainStrength(float strength);

        /**
         * Sets the strength of the thunder. This value must be equal to or greater than
         * zero. If the strength is zero, the thunder will not be displayed. A value of one
         * is the highest value sent in vanilla, but higher values are possible.
         *
         * <p>This value is ignored if the type is not {@link Type#THUNDER}.</p>
         *
         * @param strength the strength
         * @return {@code this}, for chaining
         * @throws IllegalArgumentException if {@code strength} is less than zero
         */
        @Contract("_ -> this")
        @NotNull Builder thunderStrength(float strength);

        /**
         * Creates a weather instance from this builder.
         *
         * @return the weather instance
         */
        @Contract("-> new")
        @NotNull Weather build();
    }
}
