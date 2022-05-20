package org.vaadin.tltv.gantt.model;

import java.util.Objects;

public class SubStep extends GanttStep {

	private Step owner;

	public SubStep(Step owner) {
		setSubstep(true);
		setOwner(Objects.requireNonNull(owner));
	}

	public Step getOwner() {
		return owner;
	}

	public void setOwner(Step owner) {
		this.owner = owner;
	}

	public void updateOwnerDatesBySubStep() {
		// update owner by changed sub-step
		if (getOwner().getStartDate().isAfter(getStartDate())) {
			getOwner().setStartDate(getStartDate());
		} else if (getOwner().getEndDate().isBefore(getEndDate())) {
			getOwner().setEndDate(getEndDate());
		}
	}
}
