package org.vaadin.tltv.gantt.model;

public class SubStep extends GanttStep {

    private Step owner;

    public Step getOwner() {
        return owner;
    }

    public void setOwner(Step owner) {
        this.owner = owner;
    }
}
