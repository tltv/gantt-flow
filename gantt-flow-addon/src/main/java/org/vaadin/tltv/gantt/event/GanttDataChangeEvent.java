package org.vaadin.tltv.gantt.event;

import java.util.stream.Stream;

import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.model.Step;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Event that is fired when data in Gantt data model has been changed.
 */
public class GanttDataChangeEvent extends ComponentEvent<Gantt> {

	public static enum DataEvent {
		STEP_ADD, STEP_REMOVE, STEP_MOVE;
	}

	private final DataEvent dataEvent;
	private final Stream<Step> steps;

	
	public GanttDataChangeEvent(Gantt source, DataEvent dataEvent, Stream<Step> steps) {
		super(source, false);
		this.dataEvent = dataEvent;
		this.steps = steps;
	}

	public DataEvent getDataEvent() {
		return dataEvent;
	}

	public Stream<Step> getSteps() {
		return steps;
	}
}
