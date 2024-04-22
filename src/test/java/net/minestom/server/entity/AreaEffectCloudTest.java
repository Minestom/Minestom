package net.minestom.server.entity;

import net.minestom.server.color.Color;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AreaEffectCloudTest {
    @Test
    public void createWithDustParticle() {
        int colour = 0x5505FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        float size = 0.1f;

        Particle particle = Particle.DUST.withData(new DustParticleData(new Color(r, g, b), size));

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
        int colour = 0xFF05FF01;
        int colourAfter = 0xFF05FF01;

        int b = (colour & 0x000000FF);
        int g = (colour & 0x0000FF00) >> 8;
        int r = (colour & 0x00FF0000) >> 16;

        int b2 = (colourAfter & 0x000000FF);
        int g2 = (colourAfter & 0x0000FF00) >> 8;
        int r2 = (colourAfter & 0x00FF0000) >> 16;

        float size = 0.1f;

        Particle particle = Particle.DUST_COLOR_TRANSITION.withData(new DustColorTransitionParticleData(new Color(r, g, b), size, new Color(r2, g2, b2)));

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
        Block block = Block.GRASS_BLOCK;
        Particle particle = Particle.BLOCK.withData(new BlockParticleData(block));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        BlockParticleData gotBlock = (BlockParticleData) gotParticle.data();
        assert gotBlock.block() == block;
    }

    @Test
    public void createWithBlockMarkerParticle() {
        Block block = Block.GRASS_BLOCK;
        Particle particle = Particle.BLOCK_MARKER.withData(new BlockMarkerParticleData(block));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        BlockMarkerParticleData gotBlock = (BlockMarkerParticleData) gotParticle.data();
        assert gotBlock.block() == block;
    }

    @Test
    public void createWithItemParticle() {
        Particle particle = Particle.ITEM.withData(new ItemParticleData(ItemStack.of(Material.ACACIA_LOG)));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        ItemParticleData gotBlock = (ItemParticleData) gotParticle.data();
        assert gotBlock.item().material() == Material.ACACIA_LOG;
    }

    @Test
    public void createWithSculkChargeParticle() {
        Particle particle = Particle.SCULK_CHARGE.withData(new SculkChargeParticleData(3));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        SculkChargeParticleData gotBlock = (SculkChargeParticleData) gotParticle.data();
        assert gotBlock.roll() == 3;
    }

    @Test
    public void createWithDustParticleIncorrectType() {
        Particle particle = Particle.DUST.withData(new FallingDustParticleData(Block.GLOWSTONE));

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);
        assertThrows(IllegalStateException.class, () -> entity.getMetadataPacket().write(new NetworkBuffer()));
    }

    @Test
    public void createWithComposterParticle() {
        Particle particle = Particle.COMPOSTER;

        Entity entity = new Entity(EntityTypes.AREA_EFFECT_CLOUD);
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) entity.getEntityMeta();
        meta.setParticle(particle);

        var gotParticle = meta.getParticle();
        assert gotParticle == particle;

        ParticleData gotBlock = gotParticle.data();
        assertNull(gotBlock);
    }
}
