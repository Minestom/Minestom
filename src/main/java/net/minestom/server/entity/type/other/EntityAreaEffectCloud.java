package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityAreaEffectCloud extends ObjectEntity {

    public Consumer<BinaryWriter> particleDataConsumer;
    private float radius;
    private int color;
    private boolean ignoreRadius;
    private Particle particle;

    public EntityAreaEffectCloud(Position spawnPosition) {
        super(EntityType.AREA_EFFECT_CLOUD, spawnPosition);
        setRadius(0.5f);
        setColor(0);
        setIgnoreRadius(false);
        setParticle(Particle.EFFECT);
        setParticleDataConsumer(packetWriter -> {
        });
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 7);
            fillMetadataIndex(packet, 8);
            fillMetadataIndex(packet, 9);
            fillMetadataIndex(packet, 10);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_FLOAT);
            packet.writeFloat(radius);
        } else if (index == 8) {
            packet.writeByte((byte) 8);
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(color);
        } else if (index == 9) {
            packet.writeByte((byte) 9);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(ignoreRadius);
        } else if (index == 10) {
            packet.writeByte((byte) 10);
            packet.writeByte(METADATA_PARTICLE);
            packet.writeVarInt(particle.getId());
            particleDataConsumer.accept(packet);
        }
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        setBoundingBox(2 * radius, 0.5f, 2 * radius);
        sendMetadataIndex(7);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        sendMetadataIndex(8);
    }

    public boolean isIgnoreRadius() {
        return ignoreRadius;
    }

    public void setIgnoreRadius(boolean ignoreRadius) {
        this.ignoreRadius = ignoreRadius;
        sendMetadataIndex(9);
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
        sendMetadataIndex(10);
    }

    public Consumer<BinaryWriter> getParticleDataConsumer() {
        return particleDataConsumer;
    }

    /**
     * Used to add data to the particle
     *
     * @param particleDataConsumer the particle data consumer
     * @see <a href="https://wiki.vg/Data_types#Particle">Particle data</a>
     */
    public void setParticleDataConsumer(Consumer<BinaryWriter> particleDataConsumer) {
        this.particleDataConsumer = particleDataConsumer;
    }
}
