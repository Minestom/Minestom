package net.minestom.server.weather;

import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

final class WeatherImpl implements Weather {
    static final Weather INFINITELY_CLEAR = new WeatherImpl(Type.CLEAR), INFINITELY_RAINING = new WeatherImpl(Type.RAIN),
            INFINITELY_THUNDERING = new WeatherImpl(Type.THUNDER);

    private final Type type;
    private final Instant expiration;
    private final float rainStrength, thunderStrength;

    private WeatherImpl(@NotNull Type type) {
        this.type = type;
        this.expiration = null;
        this.rainStrength = 1;
        this.thunderStrength = 1;
    }

    WeatherImpl(@NotNull BuilderImpl builder) {
        this.type = builder.type;
        this.expiration = builder.expiration;
        this.rainStrength = builder.rainStrength;
        this.thunderStrength = builder.thunderStrength;
    }

    WeatherImpl(@NotNull Type type, @NotNull Duration length) {
        Objects.requireNonNull(length, "length");
        if (length.isNegative()) throw new IllegalArgumentException("length cannot be negative");

        this.type = Objects.requireNonNull(type, "type");
        this.expiration = Instant.now().plus(length);
        this.rainStrength = 1;
        this.thunderStrength = 1;
    }

    @Override
    public @NotNull Type getType() {
        return this.type;
    }

    @Override
    public @Nullable Duration getRemainingDuration() {
        if (this.expiration == null) {
            return null;
        } else {
            final Instant now = Instant.now();

            if (now.isAfter(this.expiration)) {
                return Duration.ZERO;
            } else {
                return Duration.between(now, this.expiration);
            }
        }
    }

    @Override
    public boolean hasExpired() {
        return this.expiration != null && this.expiration.isBefore(Instant.now());
    }

    @Override
    public float getRainStrength() {
        return this.rainStrength;
    }

    @Override
    public float getThunderStrength() {
        return this.thunderStrength;
    }

    @Override
    public @NotNull Instant getExpiration() {
        return this.expiration;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public boolean equalsIgnoreExpiration(@NotNull Weather that) {
        return this.type == that.getType() &&
                (this.type == Type.CLEAR || MathUtils.equals(this.rainStrength, that.getRainStrength())) &&
                (this.type == Type.THUNDER || MathUtils.equals(this.thunderStrength, that.getThunderStrength()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherImpl)) return false;
        WeatherImpl that = (WeatherImpl) o;
        return this.equalsIgnoreExpiration(that) && Objects.equals(expiration, that.expiration);
    }

    @Override
    public int hashCode() {
        switch (this.type) {
            case CLEAR: return Objects.hash(this.type, this.expiration);
            case RAIN: return Objects.hash(this.type, this.expiration, this.rainStrength);
            case THUNDER: return Objects.hash(this.type, this.expiration, this.rainStrength, this.thunderStrength);
            default: throw new IllegalStateException("unknown weather type");
        }
    }

    static final class BuilderImpl implements Builder {
        private Type type;
        private Instant expiration;
        private float rainStrength, thunderStrength;

        BuilderImpl() {
            this.type = Type.CLEAR;
            this.expiration = null;
            this.rainStrength = 1;
            this.thunderStrength = 1;
        }

        private BuilderImpl(@NotNull WeatherImpl weather) {
            this.type = weather.type;
            this.expiration = weather.expiration;
            this.rainStrength = weather.rainStrength;
            this.thunderStrength = weather.thunderStrength;
        }

        @Override
        public @NotNull Builder type(@NotNull Type type) {
            this.type = Objects.requireNonNull(type, "type");
            return this;
        }

        @Override
        public @NotNull Builder expiration(@NotNull Instant expiration) {
            Objects.requireNonNull(expiration, "expiration");
            if (Instant.now().isAfter(expiration)) throw new IllegalArgumentException("expiration cannot be in the past");
            this.expiration = expiration;
            return this;
        }

        @Override
        public @NotNull Builder infinite() {
            this.expiration = null;
            return this;
        }

        @Override
        public @NotNull Builder rainStrength(float strength) {
            if (strength < 0) throw new IllegalArgumentException("strength must be greater than or equal to zero");
            this.rainStrength = strength;
            return this;
        }

        @Override
        public @NotNull Builder thunderStrength(float strength) {
            if (strength < 0) throw new IllegalArgumentException("strength must be greater than or equal to zero");
            this.thunderStrength = strength;
            return this;
        }

        @Override
        public @NotNull Weather build() {
            return new WeatherImpl(this);
        }
    }
}
