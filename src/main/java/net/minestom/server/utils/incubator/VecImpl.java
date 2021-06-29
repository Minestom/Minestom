package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

class VecImpl {

    static class Vec3 implements Vec {
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
        public @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public double x() {
            return x;
        }

        @Override
        public double y() {
            return y;
        }

        @Override
        public double z() {
            return z;
        }
    }

    static class Tuple implements Vec {
        private final double x, z;

        Tuple(double x, double z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public double x() {
            return x;
        }

        @Override
        public double y() {
            return 0;
        }

        @Override
        public double z() {
            return z;
        }
    }

    static class Single implements Vec {
        private final double value;

        Single(double value) {
            this.value = value;
        }

        @Override
        public @NotNull Vec with(double x, double y, double z) {
            return new Vec3(x, y, z);
        }

        @Override
        public double x() {
            return value;
        }

        @Override
        public double y() {
            return value;
        }

        @Override
        public double z() {
            return value;
        }
    }
}
