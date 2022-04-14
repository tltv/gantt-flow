package org.vaadin.tltv.gantt.element;

import java.time.LocalDateTime;

import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

@Tag("gantt-step-element")
public class StepElement extends Component {

	private final String uid;
	
	private GanttStep model;
	
	public StepElement(GanttStep model) {
		this.model = model;
		this.uid = model.getUid();
		
		getElement().setProperty("uid", this.uid);
		setCaption(model.getCaption());
		setBackgroundColor(model.getBackgroundColor());
		setStartDateTime(model.getStartDate());
		setEndDateTime(model.getEndDate());
	}
	
	public String getUid() {
		return uid;
	}
	
	public GanttStep getModel() {
		return model;
	}

	public void setCaption(String caption) {
		getElement().setAttribute("caption", caption);
	}
	
	public String getCaption() {
		return getElement().getAttribute("caption");
	}
	
	public void setBackgroundColor(String backgroundColor) {
		getElement().setAttribute("backgroundColor", backgroundColor);
	}
	
	public String getBackgroundColor() {
		return getElement().getAttribute("backgroundColor");
	}
	
	public void setStartDateTime(LocalDateTime startDateTime) {
		getElement().setAttribute("start",
				GanttUtil.formatDateTime(GanttUtil.resetTimeToMin(startDateTime, Resolution.Hour)));
	}

	public LocalDateTime getStartDateTime() {
		return GanttUtil.parseLocalDateTime(getElement().getAttribute("start"));
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		getElement().setAttribute("end",
				GanttUtil.formatDateTime(GanttUtil.resetTimeToMin(endDateTime, Resolution.Hour)));
	}

	public LocalDateTime getEndDateTime() {
		return GanttUtil.parseLocalDateTime(getElement().getAttribute("end"));
	}

	public void removeFromParent() {
		getElement().removeFromParent();
	}
}
