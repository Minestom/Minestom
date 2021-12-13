package net.minestom.server.world.biomes;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public record BiomeParticle(float probability, Option option) {
    public NBTCompound toNbt() {
        return NBT.Compound(Map.of(
                "probability", NBT.Float(probability),
                "options", option.toNbt()));
    }

    public interface Option {
        NBTCompound toNbt();
    }

    public record BlockOption(Block block) implements Option {
        //TODO also can be falling_dust
        private static final String type = "block";

        @Override
        public NBTCompound toNbt() {
            return NBT.Compound(nbtCompound -> {
                nbtCompound.setString("type", type);
                nbtCompound.setString("Name", block.name());
                Map<String, String> propertiesMap = block.properties();
                if (propertiesMap.size() != 0) {
                    nbtCompound.set("Properties", NBT.Compound(p -> {
                        propertiesMap.forEach(p::setString);
                    }));
                }
            });
        }
    }

    public record DustOption(float red, float green, float blue, float scale) implements Option {
        private static final String type = "dust";

        @Override
        public NBTCompound toNbt() {
            return NBT.Compound(Map.of(
                    "type", NBT.String(type),
                    "r", NBT.Float(red),
                    "g", NBT.Float(green),
                    "b", NBT.Float(blue),
                    "scale", NBT.Float(scale)));
        }
    }

    public record ItemOption(ItemStack item) implements Option {
        private static final String type = "item";

        @Override
        public NBTCompound toNbt() {
            //todo test count might be wrong type
            NBTCompound nbtCompound = item.getMeta().toNBT();
            return nbtCompound.modify(n -> {
                n.setString("type", type);
            });
        }
    }

    public record NormalOption(NamespaceID type) implements Option {
        @Override
        public NBTCompound toNbt() {
            return NBT.Compound(Map.of("type", NBT.String(type.toString())));
        }
    }
}
