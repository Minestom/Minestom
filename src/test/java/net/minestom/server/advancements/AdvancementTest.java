package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancementTest {
    @Test
    public void addSingleCriterion() {
        Advancement advancement = new Advancement(
                Component.text("test1"),
                Component.text("test2"),
                Material.GOLDEN_HOE,
                FrameType.TASK,
                1, 0
        );

        assertEquals(0, advancement.getCriteriaList().size());
        assertEquals(0, advancement.getRequirements().size());

        advancement.addCriterion("minestom:test_criterion", true);

        assertEquals(1, advancement.getCriteriaList().size());
        Criterion criterion = advancement.getCriterion("minestom:test_criterion");

        assertNotNull(criterion);
        assertEquals("minestom:test_criterion", criterion.identifier());
        assertSame(advancement, criterion.getHandle());
        assertTrue(criterion.isAchieved());

        assertEquals(1, advancement.getRequirements().size());
        assertEquals(1, advancement.getRequirements().get(0).size());
        assertTrue(advancement.getRequirements().get(0).contains("minestom:test_criterion"));

        advancement.setAchieved("minestom:test_criterion", false);
        assertFalse(criterion.isAchieved());
        assertFalse(advancement.isAchieved("minestom:test_criterion"));

        advancement.removeCriterion("minestom:test_criterion");

        assertEquals(0, advancement.getCriteriaList().size());
        assertNull(advancement.getCriterion("minestom:test_criterion"));

        assertEquals(0, advancement.getRequirements().size());
    }

    @Test
    public void addMultiCriteria() {
        Advancement advancement = new Advancement(
                Component.text("test1"),
                Component.text("test2"),
                Material.GOLDEN_HOE,
                FrameType.TASK,
                1, 0
        );

        String[] criteria = new String[] {"minestom:criterion_test1", "minestom:criterion_test2", "minestom:criterion_test3"};

        advancement.addCriteriaToSameRequirement(criteria);

        assertEquals(3, advancement.getCriteriaList().size());
        for (String identifier : criteria) {
            Criterion criterion = advancement.getCriterion(identifier);
            assertNotNull(criterion);
            assertEquals(identifier, criterion.identifier());
            assertSame(advancement, criterion.getHandle());
            assertFalse(criterion.isAchieved());
        }

        assertEquals(1, advancement.getRequirements().size());
        List<String> requirement = advancement.getRequirements().get(0);
        assertEquals(3, requirement.size());
        for (String identifier : criteria) {
            assertTrue(requirement.contains(identifier));
        }

        advancement.removeCriterion("minestom:criterion_test2");

        assertEquals(2, advancement.getCriteriaList().size());
        assertEquals(1, advancement.getRequirements().size());
        assertEquals(2, advancement.getRequirements().get(0).size());
    }
}
