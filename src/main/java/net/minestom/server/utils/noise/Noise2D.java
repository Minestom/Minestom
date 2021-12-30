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

    static Noise2D combine(Noise2D noiseA, Noise2D noiseB) {
        return ((x, y) -> (noiseA.getValue(x,y)+ noiseB.getValue(x,y))/2);
    }

    static Noise2D map(Noise2D noise, double slope, double min) {
        return ((x, y) -> slope * (noise.getValue(x, y) - min));
    }
}
