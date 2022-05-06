package net.minestom.server.advancements;

import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import org.jetbrains.annotations.NotNull;

public class Criterion {
    private final Advancement handle;
    private final String criteriaIdentifier;
    private boolean achieved;

    protected Criterion(@NotNull Advancement handle, @NotNull String criteriaIdentifier, boolean achieved) {
        this.handle = handle;
        this.criteriaIdentifier = criteriaIdentifier;
        this.achieved = achieved;
    }

    public String identifier() {
        return criteriaIdentifier;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
        this.handle.update();
    }

    public Advancement getHandle() {
        return this.handle;
    }

    public AdvancementsPacket.Criteria toCriteriaPacket() {
        return new AdvancementsPacket.Criteria(criteriaIdentifier,
                new AdvancementsPacket.CriterionProgress(achieved ? System.currentTimeMillis() : null)
        );
    }
}
