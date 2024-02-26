package net.minestom.server.entity;

import net.minestom.server.color.Color;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.BlockParticleData;
import net.minestom.server.particle.data.DustColorTransitionParticleData;
import net.minestom.server.particle.data.DustParticleData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AreaEffectCloudTest {
    @Test
    public void createWithDustParticle() {
        Particle particle = Particle.fromNamespaceId("minecraft:dust");
        assertNotNull(particle);

        int colour = 0x5505FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        float size = 0.1f;

        particle = particle.withData(new DustParticleData(new Color(r, g, b), size));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        DustParticleData gotData = (DustParticleData) gotParticle.data();
        assertNotNull(gotData);
        assert gotData.color().red() == r;
        assert gotData.color().green() == g;
        assert gotData.color().blue() == b;
        assert gotData.scale() == size;
    }

    @Test
    public void createWithDustTransition() {
        Particle particle = Particle.fromNamespaceId("minecraft:dust_color_transition");
        assertNotNull(particle);

        int colour = 0xFF05FF01;
        int colourAfter = 0xFF05FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        int b2 = (colourAfter & 0x000000FF);
        int g2 = (colourAfter & 0x0000FF00) >> 8;
        int r2 = (colourAfter & 0x00FF0000) >> 16;

        float size = 0.1f;

        particle = particle.withData(new DustColorTransitionParticleData(new Color(r, g, b), size, new Color(r2, g2, b2)));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        DustColorTransitionParticleData gotData = (DustColorTransitionParticleData) gotParticle.data();
        assertNotNull(gotData);
        assert gotData.from().red() == r;
        assert gotData.from().green() == g;
        assert gotData.from().blue() == b;
        assert gotData.scale() == size;
        assert gotData.to().red() == r2;
        assert gotData.to().green() == g2;
        assert gotData.to().blue() == b2;
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
