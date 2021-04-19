package net.minestom.server.particle.shapes;

import net.minestom.server.entity.Player;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;

import java.util.Iterator;

public class Coords2 extends CoordinateHolder {
    private final double x1, y1, z1;
    private final double x2, y2, z2;

    public Coords2(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Coords2(Position position1, Position position2) {
        this(position1.getX(), position1.getY(), position1.getZ(), position2.getX(), position2.getY(), position2.getZ());
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getZ1() {
        return z1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public double getZ2() {
        return z2;
    }

    public LineIterator line(int particleCount) {
        return new LineIterator(this, particleCount);
    }

    public static class LineIterator extends ParticleIterator<Coords2> implements Iterator<Position> {
        private final double changeX, changeY, changeZ;

        private double x, y, z;

        private int particles = 0;

        public LineIterator(Coords2 line, int particleCount) {
            super(line, particleCount);

            double dx = line.getX2() - line.getX1();
            double dy = line.getY2() - line.getY1();
            double dz = line.getZ2() - line.getZ1();

            this.changeX = dx / particleCount;
            this.changeY = dy / particleCount;
            this.changeZ = dz / particleCount;

            this.x = line.getX1();
            this.y = line.getY1();
            this.z = line.getZ1();
        }

        @Override
        public boolean hasNext() {
            return particles < particleCount;
        }

        @Override
        public Position next() {
            Position position = new Position(x, y, z);

            x += changeX;
            y += changeY;
            z += changeZ;

            particles++;

            return position;
        }

        public void draw(Player player) {
            while (hasNext()) {
                Position position = next();
                player.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(Particle.FLAME,
                        position.getX(), position.getY(), position.getZ(),
                        0, 0, 0, 1));
            }
        }
    }
}
