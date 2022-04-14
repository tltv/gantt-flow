package org.vaadin.tltv.gantt.event;

import java.time.LocalDateTime;

import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("ganttStepMove")
public class StepMoveEvent extends ComponentEvent<Gantt> {

	private final String uid;
	private final String newUid;
	private final LocalDateTime start;
	private final LocalDateTime end;
	
	public StepMoveEvent(Gantt source, boolean fromClient, 
			@EventData("event.detail.uid") String uid,
			@EventData("event.detail.newUid") String newUid,
			@EventData("event.detail.start") String start,
			@EventData("event.detail.end") String end) {
		super(source, fromClient);
		this.uid = uid;
		this.newUid = newUid;
		this.start = GanttUtil.parseLocalDateTime(start);
		this.end = GanttUtil.parseLocalDateTime(end);
	}

	public GanttStep getAnyStep() {
		return getSource().getAnyStep(uid);
	}
	
	public LocalDateTime getStart() {
		return start;
	}
	
	public LocalDateTime getEnd() {
		return end;
	}
	
	public String getNewUid() {
		return newUid;
	}
}
