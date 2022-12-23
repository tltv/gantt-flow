package org.vaadin.tltv.gantt.event;

import java.time.LocalDateTime;

import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("ganttBackgroundClick")
public class GanttClickEvent extends ComponentEvent<Gantt> {

	private final Integer index;
	private final Integer button;
	private final LocalDateTime date;
	
	public GanttClickEvent(Gantt source, boolean fromClient, 
			@EventData("event.detail.index") Integer index,
			@EventData("event.detail.date") String date,
			@EventData("event.detail.event.button") Integer button) {
		super(source, fromClient);
		this.index = index;
		this.button = button;
		this.date = GanttUtil.parseLocalDateTime(date);
	}

	public Integer getIndex() {
		return index;
	}

	public Integer getButton() {
		return button;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
}
