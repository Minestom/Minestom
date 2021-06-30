package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

final class VecImpl {
    static final class Vec3 implements Vec {
        private final double x, y, z;

        Vec3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3(double value) {
            this(value, value, value);
        }

        @Override
        public final @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public final double x() {
            return x;
        }

        @Override
        public final double y() {
            return y;
        }

        @Override
        public final double z() {
            return z;
        }
    }

    static final class Tuple implements Vec {
        private final double x, z;

        Tuple(double x, double z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public final @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public final double x() {
            return x;
        }

        @Override
        public final double y() {
            return 0;
        }

        @Override
        public final double z() {
            return z;
        }
    }

    static final class Single implements Vec {
        private final double value;

        Single(double value) {
            this.value = value;
        }

        @Override
        public final @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public final double x() {
            return value;
        }

        @Override
        public final double y() {
            return value;
        }

        @Override
        public final double z() {
            return value;
        }
    }
}
