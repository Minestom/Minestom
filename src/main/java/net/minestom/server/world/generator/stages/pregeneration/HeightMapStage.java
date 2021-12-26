package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class HeightMapStage implements PreGenerationStage<HeightMapStage.Data> {
    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final Data data = new Data(30);
        context.setChunkData(data, sectionX, sectionZ);
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public int getUniqueId() {
        return INTERNAL_STAGE_ID_OFFSET+1;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }

    @Override
    public Function<BinaryReader, Data> getDataReader() {
        return Data::new;
    }

    public static class Data implements StageData.Chunk {
        public Data(int height) {
            this.height = height;
        }

        public Data(BinaryReader reader) {
            this.height = reader.readInt();
        }

        private int height;

        private void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public boolean supportsSaving() {
            return true;
        }

        @Override
        public boolean generated() {
            return false;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeInt(height);
        }
    }
}
