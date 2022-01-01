package net.minestom.server.instance;

import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.units.ChunkGenerationRequest;
import net.minestom.server.instance.generator.units.ChunkGenerationResponse;

/**
 * Provides full compatibility for the deprecated {@link ChunkGenerator}
 */
class ChunkGeneratorCompatibilityLayer implements Generator<ChunkGenerationRequest, ChunkGenerationResponse> {
    private final ChunkGenerator chunkGenerator;

    public ChunkGeneratorCompatibilityLayer(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public ChunkGenerationResponse generate(Instance instance, ChunkGenerationRequest request) {
        return null;
    }

    @Override
    public Class<ChunkGenerationRequest> supportedRequestType() {
        return ChunkGenerationRequest.class;
    }


//    @Override
//    public List<CompletableFuture<SectionResult>> generate(Instance instance, GenerationRequest request) {
//        var chunkSections = new HashMap<Point, Set<Point>>();
//        switch (request.unit()) {
//            case CHUNK -> {
//                for (Point location : request.locations()) {
//                    chunkSections.put(location, new HashSet<>());
//                    for (int y = instance.getSectionMinY(); y <= instance.getSectionMaxY(); y++) {
//                        chunkSections.get(location).add(new Vec(location.x(), y, location.z()));
//                    }
//                }
//            }
//            case SECTION -> {
//                for (Point location : request.locations()) {
//                    chunkSections.computeIfAbsent(location.withY(0), k -> new HashSet<>()).add(location);
//                }
//            }
//        }
//        var futures = new ArrayList<CompletableFuture<SectionResult>>();
//        for (Map.Entry<Point, Set<Point>> entry : chunkSections.entrySet()) {
//            final CompatibilityChunk chunk = new CompatibilityChunk(instance, (int) entry.getKey().x(), (int) entry.getKey().z());
//            final ChunkGenerationBatch batch = new ChunkGenerationBatch((InstanceContainer) instance, chunk);
//            final ArrayList<CompletableFuture<SectionResult>> chunkFutures = new ArrayList<>();
//            for (int i = 0; i < entry.getValue().size(); i++) {
//                final CompletableFuture<SectionResult> future = new CompletableFuture<>();
//                futures.add(future);
//                chunkFutures.add(future);
//            }
//            batch.generate(chunkGenerator).thenAccept(c -> {
//                int i = 0;
//                for (Point point : entry.getValue()) {
//                    chunkFutures.get(i++).complete(new SectionResult(((CompatibilityChunk)c).getData((int) point.y()), point));
//                }
//            });
//        }
//        return futures;
//    }

    /**
     * Used to provide compatibility for old generators
     */
//    private static class CompatibilityChunk extends Chunk {
//
//
//        private static final RuntimeException UNSUPPORTED_OPERATION = new UnsupportedOperationException("Operation not supported for CompatibilityChunk!");
//        private final List<Section> sections;
//        private final List<SectionData> data;
//
//        public CompatibilityChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
//            super(instance, chunkX, chunkZ, true);
//            var sectionsTemp = new Section[instance.getSectionMaxY() - instance.getSectionMinY()];
//            Arrays.setAll(sectionsTemp, value -> new Section());
//            this.sections = List.of(sectionsTemp);
//            var dataTemp = new SectionData[instance.getSectionMaxY() - instance.getSectionMinY()];
//            Arrays.setAll(dataTemp, value -> new SectionData(new SectionBlockCache(), Palette.biomes()));
//            this.data = List.of(dataTemp);
//        }
//
//        private GeneratedData getData(int y) {
//            final Section section = sections.get(y - instance.getSectionMinY());
//            final SectionData sectionData = data.get(y - instance.getSectionMinY());
//            if (section.getSkyLight().length == 0 && section.getBlockLight().length == 0) {
//                return new LegacySectionData(sectionData.blockCache(), sectionData.biomePalette(), section.getBlockLight(), section.getSkyLight());
//            } else {
//                return sectionData;
//            }
//        }
//
//        @Override
//        public void setBlock(int x, int y, int z, @NotNull Block block) {
//            data.get(ChunkUtils.getChunkCoordinate(y) - instance.getSectionMinY()).blockCache().setBlock(x, y, z, block);
//        }
//
//        @Override
//        public @NotNull List<Section> getSections() {
//            return sections;
//        }
//
//        @Override
//        public @NotNull Section getSection(int section) {
//            return sections.get(section - instance.getSectionMinY());
//        }
//
//        @Override
//        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
//            return data.get(ChunkUtils.getChunkCoordinate(y) - instance.getSectionMinY()).blockCache()
//                    .getBlock(ChunkUtils.toSectionRelativeCoordinate(x), ChunkUtils.toSectionRelativeCoordinate(y),
//                            ChunkUtils.toSectionRelativeCoordinate(z), condition);
//        }
//
//        @Override
//        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
//            data.get(ChunkUtils.getChunkCoordinate(y) - instance.getSectionMinY()).biomePalette().set(ChunkUtils.toSectionRelativeCoordinate(x),
//                    ChunkUtils.toSectionRelativeCoordinate(y), ChunkUtils.toSectionRelativeCoordinate(z), biome.id());
//        }
//
//        @Override
//        public @NotNull Biome getBiome(int x, int y, int z) {
//            return MinecraftServer.getBiomeManager().getById(data.get(ChunkUtils.getChunkCoordinate(y) - instance.getSectionMinY()).biomePalette()
//                    .get(ChunkUtils.toSectionRelativeCoordinate(x), ChunkUtils.toSectionRelativeCoordinate(y),
//                            ChunkUtils.toSectionRelativeCoordinate(z)));
//        }
//
//        //region Unsupported operations
//
//        @Override
//        public boolean addViewer(@NotNull Player player) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public boolean removeViewer(@NotNull Player player) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public @NotNull Set<Player> getViewers() {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public void setSection(GeneratedData sectionData, int y) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public void tick(long time) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public long getLastChangeTime() {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public void sendChunk(@NotNull Player player) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public void sendChunk() {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        @Override
//        public void reset() {
//            throw UNSUPPORTED_OPERATION;
//        }
//
//        //endregion
//    }
}
