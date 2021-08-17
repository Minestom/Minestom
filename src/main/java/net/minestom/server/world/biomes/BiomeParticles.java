package net.minestom.server.world.biomes;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public class BiomeParticles {

    private final float probability;
    private final ParticleOptions options;

    public BiomeParticles(float probability, ParticleOptions options) {
        this.probability = probability;
        this.options = options;
    }

    public NBTCompound toNbt() {
        NBTCompound nbt = new NBTCompound();
        nbt.setFloat("probability", probability);
        nbt.set("options", options.toNbt());
        return nbt;
    }

    public interface ParticleOptions {
        NBTCompound toNbt();
    }

    public static class BlockParticle implements ParticleOptions {

        //TODO also can be falling_dust
        private static final String type = "block";

        private final Block block;

        public BlockParticle(Block block) {
            this.block = block;
        }

        @Override
        public NBTCompound toNbt() {
            NBTCompound nbtCompound = new NBTCompound();
            nbtCompound.setString("type", type);
            nbtCompound.setString("Name", block.name());
            Map<String, String> propertiesMap = block.properties();
            if (propertiesMap.size() != 0) {
                NBTCompound properties = new NBTCompound();
                propertiesMap.forEach(properties::setString);
                nbtCompound.set("Properties", properties);
            }
            return nbtCompound;
        }

    }

    public static class DustParticle implements ParticleOptions {

        private static final String type = "dust";

        private final float red;
        private final float green;
        private final float blue;
        private final float scale;

        public DustParticle(float red, float green, float blue, float scale) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.scale = scale;
        }

        @Override
        public NBTCompound toNbt() {
            NBTCompound nbtCompound = new NBTCompound();
            nbtCompound.setString("type", type);
            nbtCompound.setFloat("r", red);
            nbtCompound.setFloat("g", green);
            nbtCompound.setFloat("b", blue);
            nbtCompound.setFloat("scale", scale);
            return nbtCompound;
        }

    }

    public static class ItemParticle implements ParticleOptions {

        private static final String type = "item";

        private final ItemStack item;

        public ItemParticle(ItemStack item) {
            this.item = item;
        }

        @Override
        public NBTCompound toNbt() {
            //todo test count might be wrong type
            NBTCompound nbtCompound = item.getMeta().toNBT();
            nbtCompound.setString("type", type);
            return nbtCompound;
        }

    }

    public static class NormalParticle implements ParticleOptions {

        private final NamespaceID type;

        public NormalParticle(@NotNull NamespaceID type) {
            this.type = type;
        }

        @Override
        public NBTCompound toNbt() {
            NBTCompound nbtCompound = new NBTCompound();
            nbtCompound.setString("type", type.toString());
            return nbtCompound;
        }

    }
}
