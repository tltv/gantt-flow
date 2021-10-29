package org.vaadin.tltv.gantt.event;

import org.vaadin.tltv.gantt.Gantt;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("ganttBackgroundClick")
public class GanttClickEvent extends ComponentEvent<Gantt> {

	private final Integer index;
	
	public GanttClickEvent(Gantt source, boolean fromClient, @EventData("event.detail.index") Integer index) {
		super(source, fromClient);
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}

}
