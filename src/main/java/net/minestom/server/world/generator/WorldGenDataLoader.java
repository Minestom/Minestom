package net.minestom.server.world.generator;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Nullable;

public interface WorldGenDataLoader {
    static WorldGenDataLoader dummy() {
        return new WorldGenDataLoader() {
            @Override
            public void saveInstanceData(int stageId, int stageVersion, BinaryWriter writer) {

            }

            @Override
            public void saveChunkData(int stageId, int stageVersion, int chunkX, int chunkZ, BinaryWriter writer) {

            }

            @Override
            public void saveSectionData(int stageId, int stageVersion, int sectionX, int sectionY, int sectionZ, BinaryWriter writer) {

            }

            @Override
            public BinaryReader readInstanceData(int stageId, int stageVersion) {
                return null;
            }

            @Override
            public BinaryReader readChunkData(int stageId, int stageVersion, int chunkX, int chunkZ) {
                return null;
            }

            @Override
            public BinaryReader readSectionData(int stageId, int stageVersion, int sectionX, int sectionY, int sectionZ) {
                return null;
            }
        };
    }

    void saveInstanceData(int stageId, int stageVersion, BinaryWriter writer);
    void saveChunkData(int stageId, int stageVersion, int chunkX, int chunkZ, BinaryWriter writer);
    void saveSectionData(int stageId, int stageVersion, int sectionX, int sectionY, int sectionZ, BinaryWriter writer);
    @Nullable BinaryReader readInstanceData(int stageId, int stageVersion);
    @Nullable BinaryReader readChunkData(int stageId, int stageVersion, int chunkX, int chunkZ);
    @Nullable BinaryReader readSectionData(int stageId, int stageVersion, int sectionX, int sectionY, int sectionZ);
}
