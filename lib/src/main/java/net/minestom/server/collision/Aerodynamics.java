package net.minestom.server.collision;

import it.unimi.dsi.fastutil.doubles.DoubleUnaryOperator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the aerodynamic properties of an entity
 *
 * @param gravity                 the entity's downward acceleration per tick
 * @param horizontalAirResistance the horizontal drag coefficient; the entity's current horizontal
 *                                velocity is multiplied by this every tick
 * @param verticalAirResistance   the vertical drag coefficient; the entity's current vertical
 *  *                             velocity is multiplied by this every tick
 */
public record Aerodynamics(double gravity, double horizontalAirResistance, double verticalAirResistance) {
    @Contract(pure = true)
    public @NotNull Aerodynamics withGravity(double gravity) {
        return new Aerodynamics(gravity, horizontalAirResistance, verticalAirResistance);
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withGravity(@NotNull DoubleUnaryOperator operator) {
        return withGravity(operator.apply(gravity));
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withHorizontalAirResistance(double horizontalAirResistance) {
        return new Aerodynamics(gravity, horizontalAirResistance, verticalAirResistance);
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withHorizontalAirResistance(@NotNull DoubleUnaryOperator operator) {
        return withHorizontalAirResistance(operator.apply(horizontalAirResistance));
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withVerticalAirResistance(double verticalAirResistance) {
        return new Aerodynamics(gravity, horizontalAirResistance, verticalAirResistance);
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withVerticalAirResistance(@NotNull DoubleUnaryOperator operator) {
        return withVerticalAirResistance(operator.apply(verticalAirResistance));
    }

    @Contract(pure = true)
    public @NotNull Aerodynamics withAirResistance(double horizontalAirResistance, double verticalAirResistance) {
        return new Aerodynamics(gravity, horizontalAirResistance, verticalAirResistance);
    }
}
