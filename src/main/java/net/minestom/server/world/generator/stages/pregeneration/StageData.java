package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.binary.Writeable;

public interface StageData extends Writeable {
    boolean generated();

    abstract class Instance implements StageData {}
    abstract class Chunk implements StageData {}
    abstract class Section implements StageData {}
}
