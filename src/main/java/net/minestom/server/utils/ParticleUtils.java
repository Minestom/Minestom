package net.minestom.server.utils;

import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.particle.data.ParticleData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParticleUtils {
    public static void drawParticle(@NotNull Instance instance, @NotNull Position position) {
        drawParticle(instance, position, Particle.FLAME, null);
    }

    public static void drawParticle(@NotNull Instance instance, @NotNull Position position,
                                    @NotNull Particle particle, @Nullable ParticleData data) {
        ParticlePacket packet = ParticleCreator.createParticlePacket(particle, false,
                position.getX(), position.getY(), position.getZ(),
                0, 0, 0, 0, 1, data);

        instance.getPlayers().forEach((player) ->
                player.getPlayerConnection().sendPacket(packet));
    }

    public static Position bezier(@NotNull Position[] points, double time) {
        double x = 0;
        double y = 0;
        double z = 0;

        int order = points.length - 1;
        for (int i = 0; i <= order; i++) {
            double preCompute = binomial(order, i) * Math.pow((1 - time), (order - i)) * Math.pow(time, i);

            x = x + (preCompute * points[i].getX());
            y = y + (preCompute * points[i].getY());
            z = z + (preCompute * points[i].getZ());
        }

        return new Position(x, y, z);
    }

    public static int binomial(int n, int k) {
        if (k > n - k) {
            k = n - k;
        }

        int binomial = 1;
        for (int i = 1; i <= k; i++) {
            binomial = binomial * (n + 1 - i) / i;
        }
        return binomial;
    }
}
