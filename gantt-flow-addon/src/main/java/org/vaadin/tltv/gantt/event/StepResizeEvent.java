package org.vaadin.tltv.gantt.event;

import org.vaadin.tltv.gantt.Gantt;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

@DomEvent("ganttStepResize")
public class StepResizeEvent extends ComponentEvent<Gantt> {

	public StepResizeEvent(Gantt source, boolean fromClient) {
		super(source, fromClient);
	}

}
