package org.vaadin.tltv.gantt.event;

import org.vaadin.tltv.gantt.Gantt;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

@DomEvent("ganttStepMove")
public class StepMoveEvent extends ComponentEvent<Gantt> {

	public StepMoveEvent(Gantt source, boolean fromClient) {
		super(source, fromClient);
	}

}
