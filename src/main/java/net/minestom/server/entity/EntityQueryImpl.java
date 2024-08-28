package net.minestom.server.entity;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

record EntityQueryImpl(Target target, Sort sort,
                       Range.Float distance,
                       Range.Int experience,
                       Range.Float pitchRotation,
                       Range.Float yawRotation,
                       Map<GameMode, Boolean> gameModes,
                       Map<String, Boolean> names,
                       Map<EntityType, Boolean> types,
                       Map<BinaryTag, Boolean> nbt,
                       int limit) implements EntityQuery {
    public EntityQueryImpl {
        gameModes = Map.copyOf(gameModes);
        names = Map.copyOf(names);
        types = Map.copyOf(types);
        nbt = Map.copyOf(nbt);
    }

    static final class BuilderImpl implements EntityQuery.Builder {
        private Target target;
        private Sort sort = Sort.ARBITRARY;
        private Range.Float distance;
        private Range.Int experience;
        private Range.Float pitchRotation;
        private Range.Float yawRotation;
        private Map<GameMode, Boolean> gameModes = new HashMap<>();
        private Map<String, Boolean> names = new HashMap<>();
        private Map<EntityType, Boolean> types = new HashMap<>();
        private Map<BinaryTag, Boolean> nbt = new HashMap<>();
        private int limit;

        @Override
        public @NotNull Builder target(@NotNull Target target) {
            this.target = target;
            return this;
        }

        @Override
        public @NotNull Builder sort(@NotNull Sort sort) {
            this.sort = sort;
            return this;
        }

        @Override
        public @NotNull Builder distance(Range.@NotNull Float distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public @NotNull Builder experience(Range.@NotNull Int experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public @NotNull Builder pitchRotation(Range.@NotNull Float pitchRotation) {
            this.pitchRotation = pitchRotation;
            return this;
        }

        @Override
        public @NotNull Builder yawRotation(Range.@NotNull Float yawRotation) {
            this.yawRotation = yawRotation;
            return this;
        }

        @Override
        public @NotNull Builder gameMode(GameMode gameMode, boolean include) {
            this.gameModes.put(gameMode, include);
            return this;
        }

        @Override
        public @NotNull Builder name(@NotNull String name, boolean include) {
            this.names.put(name, include);
            return this;
        }

        @Override
        public @NotNull Builder type(@NotNull EntityType type, boolean include) {
            this.types.put(type, include);
            return this;
        }

        @Override
        public @NotNull Builder nbt(@NotNull BinaryTag nbt, boolean include) {
            this.nbt.put(nbt, include);
            return this;
        }

        @Override
        public @NotNull Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public @NotNull EntityQuery build() {
            return new EntityQueryImpl(target, sort,
                    distance, experience, pitchRotation, yawRotation,
                    gameModes, names, types, nbt, limit);
        }
    }
}
