package net.minestom.server.world.biomes;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public record BiomeParticle(float probability, Option option) {
    public NBTCompound toNbt() {
        NBTCompound nbt = new NBTCompound();
        nbt.setFloat("probability", probability);
        nbt.set("options", option.toNbt());
        return nbt;
    }

    public interface Option {
        NBTCompound toNbt();
    }

    public record BlockOption(Block block) implements Option {
        //TODO also can be falling_dust
        private static final String type = "block";

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

    public record DustOption(float red, float green, float blue, float scale) implements Option {
        private static final String type = "dust";

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

    public record ItemOption(ItemStack item) implements Option {
        private static final String type = "item";

        @Override
        public NBTCompound toNbt() {
            //todo test count might be wrong type
            NBTCompound nbtCompound = item.getMeta().toNBT();
            nbtCompound.setString("type", type);
            return nbtCompound;
        }
    }

    public record NormalOption(NamespaceID type) implements Option {
        @Override
        public NBTCompound toNbt() {
            NBTCompound nbtCompound = new NBTCompound();
            nbtCompound.setString("type", type.toString());
            return nbtCompound;
        }
    }
}
