package org.vaadin.tltv.gantt.event;

import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.model.GanttStep;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("ganttStepClick")
public class StepClickEvent extends ComponentEvent<Gantt> {

	private final String uid;
	
	public StepClickEvent(Gantt source, boolean fromClient, @EventData("event.detail.uid") String uid) {
		super(source, fromClient);
		this.uid = uid;
	}

	public GanttStep getAnyStep() {
		return getSource().getAnyStep(uid);
	}
	
	public int getIndex() {
		return getSource().indexOf(uid);
	}
	
}
