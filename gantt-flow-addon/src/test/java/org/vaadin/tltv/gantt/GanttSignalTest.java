package org.vaadin.tltv.gantt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.model.SubStep;

class GanttSignalTest {

    @Test
    void addStepsFromStreamUpdatesSignalBackedModel() {
        Gantt gantt = new Gantt();
        Step first = step("first");
        Step second = step("second");

        gantt.addSteps(Stream.of(first, second));

        assertEquals(List.of(first, second), gantt.getStepsList());
        assertTrue(gantt.contains(first.getUid()));
        assertSame(second, gantt.getStep("second"));
    }

    @Test
    void movingSubstepToOwnerWithoutSubstepsCreatesTargetSignal() {
        Gantt gantt = new Gantt();
        Step originalOwner = step("original");
        Step targetOwner = step("target");
        gantt.addSteps(originalOwner, targetOwner);

        SubStep subStep = new SubStep(originalOwner);
        subStep.setUid("sub");
        subStep.setStartDate(LocalDateTime.of(2026, 1, 1, 9, 0));
        subStep.setEndDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        gantt.addSubStep(subStep);

        gantt.moveSubStep(1, subStep);

        assertSame(targetOwner, subStep.getOwner());
        assertEquals(List.of(subStep), gantt.getSubSteps().toList());
        assertSame(subStep, gantt.getSubStep("sub"));
    }

    @Test
    void removingStepUpdatesSignalBackedModel() {
        Gantt gantt = new Gantt();
        Step first = step("first");
        Step second = step("second");
        gantt.addSteps(first, second);

        assertTrue(gantt.removeStep(first));

        assertEquals(List.of(second), gantt.getStepsList());
        assertFalse(gantt.contains("first"));
    }

    private static Step step(String uid) {
        Step step = new Step();
        step.setUid(uid);
        step.setStartDate(LocalDateTime.of(2026, 1, 1, 8, 0));
        step.setEndDate(LocalDateTime.of(2026, 1, 1, 17, 0));
        return step;
    }
}
