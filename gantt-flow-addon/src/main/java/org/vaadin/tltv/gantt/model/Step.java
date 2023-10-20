package org.vaadin.tltv.gantt.model;

import org.vaadin.tltv.gantt.Gantt;

/**
 * A step inside {@link Gantt} component.
 */
public class Step extends GanttStep {

    private Step predecessor;

    public Step getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Step predecessor) {
        this.predecessor = predecessor;
    }

}
