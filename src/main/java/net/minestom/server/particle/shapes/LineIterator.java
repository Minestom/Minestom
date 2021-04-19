package net.minestom.server.particle.shapes;

import net.minestom.server.entity.Player;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;

import java.util.Iterator;

public class LineIterator extends ParticleIterator<Coords2> implements Iterator<Position> {
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
