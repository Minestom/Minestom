package net.minestom.server.world.biomes;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;

import java.util.Map;

public record BiomeParticle(float probability, Option option) {
    public CompoundBinaryTag toNbt() {
        return CompoundBinaryTag.builder()
                .putFloat("probability", probability)
                .put("options", option.toNbt())
                .build();
    }

    public interface Option {
        CompoundBinaryTag toNbt();
    }

    public record BlockOption(Block block) implements Option {
        //TODO also can be falling_dust
        private static final String type = "block";

        @Override
        public CompoundBinaryTag toNbt() {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            builder.putString("type", type);
            builder.putString("Name", block.name());
            Map<String, String> propertiesMap = block.properties();
            if (!propertiesMap.isEmpty()) {
                CompoundBinaryTag.Builder properties = CompoundBinaryTag.builder();
                propertiesMap.forEach(properties::putString);
                builder.put("Properties", properties.build());
            }
            return builder.build();
        }
    }

    public record DustOption(float red, float green, float blue, float scale) implements Option {
        private static final String type = "dust";

        @Override
        public CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", type)
                    .putFloat("r", red)
                    .putFloat("g", green)
                    .putFloat("b", blue)
                    .putFloat("scale", scale)
                    .build();
        }
    }

    public record ItemOption(ItemStack item) implements Option {
        private static final String type = "item";

        @Override
        public CompoundBinaryTag toNbt() {
            //todo test count might be wrong type
            return item.meta().toNBT()
                    .putString("type", type);
        }
    }

    public record NormalOption(NamespaceID type) implements Option {
        @Override
        public CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", type.toString())
                    .build();
        }
    }
}
