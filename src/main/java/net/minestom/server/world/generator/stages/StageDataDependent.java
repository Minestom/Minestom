package net.minestom.server.world.generator.stages;

import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public interface StageDataDependent {
    default @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Collections.emptySet();
    }
}
