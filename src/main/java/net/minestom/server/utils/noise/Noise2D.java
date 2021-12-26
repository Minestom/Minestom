package net.minestom.server.utils.noise;

@FunctionalInterface
public interface Noise2D {
    double getValue(double x, double y);

    static Noise2D scale(Noise2D base, double factor) {
        return ((x, y) -> base.getValue(x*factor, y*factor));
    }
    static Noise2D scaleX(Noise2D base, double factor) {
        return ((x, y) -> base.getValue(x*factor, y));
    }
    static Noise2D scaleY(Noise2D base, double factor) {
        return ((x, y) -> base.getValue(x, y*factor));
    }
}
