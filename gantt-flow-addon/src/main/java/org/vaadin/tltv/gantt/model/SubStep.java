package org.vaadin.tltv.gantt.model;

import java.util.Objects;

/**
 * A substep inside a {@link GanttStep} component.
 */
public class SubStep extends GanttStep {

	private Step owner;

	/**
	 * Construct a substep for the given owner {@link Step}.
	 * 
	 * @param owner {@link Step} object. Not null.
	 */
	public SubStep(Step owner) {
		setSubstep(true);
		setOwner(Objects.requireNonNull(owner));
	}

	/**
	 * Return a {@link Step} that this substep belongs to.
	 * 
	 * @return {@link Step}
	 */
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
