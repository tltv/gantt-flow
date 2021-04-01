package org.vaadin.tltv.gantt.element;

import java.time.LocalDateTime;

import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

@Tag("gantt-step-element")
public class StepElement extends Component {

	private final String uid;
	
	public StepElement(Step data) {
		this.uid = data.getUid();
		
		setCaption(data.getCaption());
		setBackgroundColor(data.getBackgroundColor());
		setStartDateTime(data.getStartDate());
		setEndDateTime(data.getEndDate());
	}
	
	public String getUid() {
		return uid;
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
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("start")));
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		getElement().setAttribute("end",
				GanttUtil.formatDateTime(GanttUtil.resetTimeToMin(endDateTime, Resolution.Hour)));
	}

	public LocalDateTime getEndDateTime() {
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("end")));
	}

	public void removeFromParent() {
		getElement().removeFromParent();
	}
}
