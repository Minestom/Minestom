package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

final class VecImpl {
    static final class Vec3 implements Vec {
        private final double x, y, z;

        Vec3(double x, double y, double z) {
            this.x = x;
            this.y = y;
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
            return y;
        }

        @Override
        public final double z() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            return VecImpl.equals(this, o);
        }

        @Override
        public int hashCode() {
            return VecImpl.hashCode(this);
        }

        @Override
        public String toString() {
            return VecImpl.toString(this);
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

        @Override
        public boolean equals(Object o) {
            return VecImpl.equals(this, o);
        }

        @Override
        public int hashCode() {
            return VecImpl.hashCode(this);
        }

        @Override
        public String toString() {
            return VecImpl.toString(this);
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

        @Override
        public boolean equals(Object o) {
            return VecImpl.equals(this, o);
        }

        @Override
        public int hashCode() {
            return VecImpl.hashCode(this);
        }

        @Override
        public String toString() {
            return VecImpl.toString(this);
        }
    }

    private static boolean equals(@NotNull Vec vec1, Object o) {
        if (vec1 == o) return true;
        if (!(o instanceof Vec)) return false;
        Vec vec2 = (Vec) o;
        return Double.compare(vec1.x(), vec2.x()) == 0 &&
                Double.compare(vec1.y(), vec2.y()) == 0 &&
                Double.compare(vec1.z(), vec2.z()) == 0;
    }

    private static int hashCode(@NotNull Vec vec) {
        return Objects.hash(vec.x(), vec.y(), vec.z());
    }

    private static @NotNull String toString(@NotNull Vec vec) {
        return "Vec3{" +
                "x=" + vec.x() +
                ", y=" + vec.y() +
                ", z=" + vec.z() +
                '}';
    }
}
