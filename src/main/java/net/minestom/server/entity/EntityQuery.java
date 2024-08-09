package net.minestom.server.entity;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public sealed interface EntityQuery permits EntityQueryImpl {

    Target target();

    @NotNull Sort sort();

    Range.Float distance();

    Range.Int experience();

    Range.Float pitchRotation();

    Range.Float yawRotation();

    Map<GameMode, Boolean> gameModes();

    Map<String, Boolean> names();

    Map<EntityType, Boolean> types();

    Map<BinaryTag, Boolean> nbt();

    int limit();

    enum Target {
        NEAREST_PLAYER, RANDOM_PLAYER,
        ALL_PLAYERS, ALL_ENTITIES,
        SELF, MINESTOM_USERNAME, MINESTOM_UUID
    }

    enum Sort {
        ARBITRARY, FURTHEST, NEAREST, RANDOM
    }

    static @NotNull Builder builder() {
        return new EntityQueryImpl.BuilderImpl();
    }

    sealed interface Builder permits EntityQueryImpl.BuilderImpl {
        @NotNull
        Builder target(@NotNull Target target);

        @NotNull
        Builder sort(@NotNull Sort sort);

        @NotNull
        Builder distance(Range.@NotNull Float distance);

        default @NotNull Builder distance(float minimum, float maximum) {
            return distance(new Range.Float(minimum, maximum));
        }

        default @NotNull Builder distance(float distance) {
            return distance(new Range.Float(distance));
        }

        @NotNull
        Builder experience(Range.@NotNull Int experience);

        @NotNull
        Builder pitchRotation(Range.@NotNull Float pitchRotation);

        @NotNull
        Builder yawRotation(Range.@NotNull Float yawRotation);

        @NotNull
        Builder gameMode(GameMode gameMode, boolean include);

        default @NotNull Builder gameMode(@NotNull Map<@NotNull GameMode, Boolean> gameModes) {
            gameModes.forEach(this::gameMode);
            return this;
        }

        default @NotNull Builder includeGameMode(@NotNull GameMode gameMode) {
            return gameMode(gameMode, true);
        }

        default @NotNull Builder excludeGameMode(@NotNull GameMode gameMode) {
            return gameMode(gameMode, false);
        }

        @NotNull
        Builder name(@NotNull String name, boolean include);

        default @NotNull Builder name(@NotNull Map<@NotNull String, Boolean> names) {
            names.forEach(this::name);
            return this;
        }

        default @NotNull Builder includeName(@NotNull String name) {
            return name(name, true);
        }

        default @NotNull Builder excludeName(@NotNull String name) {
            return name(name, false);
        }

        @NotNull
        Builder type(@NotNull EntityType type, boolean include);

        default @NotNull Builder type(@NotNull Map<@NotNull EntityType, Boolean> types) {
            types.forEach(this::type);
            return this;
        }

        default @NotNull Builder includeType(@NotNull EntityType type) {
            return type(type, true);
        }

        default @NotNull Builder excludeType(@NotNull EntityType type) {
            return type(type, false);
        }

        default @NotNull Builder playerType() {
            return type(EntityType.PLAYER, true);
        }

        @NotNull
        Builder nbt(@NotNull BinaryTag nbt, boolean include);

        default @NotNull Builder nbt(@NotNull Map<@NotNull BinaryTag, Boolean> nbtMap) {
            nbtMap.forEach(this::nbt);
            return this;
        }

        default @NotNull Builder includeNbt(@NotNull BinaryTag nbt) {
            return nbt(nbt, true);
        }

        default @NotNull Builder excludeNbt(@NotNull BinaryTag nbt) {
            return nbt(nbt, false);
        }

        @NotNull
        Builder limit(int limit);

        @NotNull
        EntityQuery build();
    }
}
