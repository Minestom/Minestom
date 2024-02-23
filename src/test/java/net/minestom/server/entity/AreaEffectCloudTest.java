package net.minestom.server.entity;

import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.BlockParticleData;
import net.minestom.server.particle.data.DustColorTransitionParticleData;
import net.minestom.server.particle.data.DustParticleData;
import org.junit.jupiter.api.Test;

public class AreaEffectCloudTest {
    @Test
    public void createWithDustParticle() {
        Particle particle = Particle.fromNamespaceId("minecraft:dust");
        int colour = 0x5505FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        float size = 0;

        particle = particle.withData(new DustParticleData((float) (r / 255.0), (float) (g / 255.0), (float) (b / 255.0), size));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        DustParticleData gotData = (DustParticleData) gotParticle.data();
        assert gotData.red() == (float) (r / 255.0);
        assert gotData.green() == (float) (g / 255.0);
        assert gotData.blue() == (float) (b / 255.0);
        assert gotData.scale() == size;
    }

    @Test
    public void createWithDustTransition() {
        Particle particle = Particle.fromNamespaceId("minecraft:dust_color_transition");
        int colour = 0x5505FF01;
        int colourAfter = 0x5505FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        int b2 = (colourAfter & 0x000000FF);
        int g2 = (colourAfter & 0x0000FF00) >> 8;
        int r2 = (colourAfter & 0x00FF0000) >> 16;

        float size = 0;

        particle = particle.withData(new DustColorTransitionParticleData((float) (r / 255.0), (float) (g / 255.0), (float) (b / 255.0), size, (float) (r2 / 255.0), (float) (g2 / 255.0), (float) (b2 / 255.0)));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        DustColorTransitionParticleData gotData = (DustColorTransitionParticleData) gotParticle.data();
        assert gotData.fromRed() == (float) (r / 255.0);
        assert gotData.fromGreen() == (float) (g / 255.0);
        assert gotData.fromBlue() == (float) (b / 255.0);
        assert gotData.toRed() == (float) (r2 / 255.0);
        assert gotData.toGreen() == (float) (g2 / 255.0);
        assert gotData.toBlue() == (float) (b2 / 255.0);
        assert gotData.scale() == size;
    }

    @Test
    public void createWithBlockParticle() {
        Particle particle = Particle.fromNamespaceId("minecraft:block");
        Block block = Block.GRASS_BLOCK;
        particle = particle.withData(new BlockParticleData(block));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        BlockParticleData gotBlock = (BlockParticleData) gotParticle.data();
        assert gotBlock.block() == block;
    }
}
