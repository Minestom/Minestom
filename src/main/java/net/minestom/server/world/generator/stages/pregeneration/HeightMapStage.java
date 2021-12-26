package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }


    public static class Data implements StageData.Chunk {
        public Data(int height) {
            this.height = height;
        }

        private int height;

        private void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public boolean generated() {
            return false;
        }
    }
}
