package org.vaadin.tltv.gantt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class GanttDemoView extends Div {

	private final Gantt gantt;
	
	public GanttDemoView() {
		setSizeFull();
		
		gantt = new Gantt();
		gantt.setResolution(Resolution.Day);
		gantt.setStartDate(LocalDate.of(2020, 4, 1));
		gantt.setEndDateTime(LocalDateTime.of(2020, 12, 1, 23, 59, 59));
		gantt.setLocale(UI.getCurrent().getLocale());
		gantt.setTimeZone(TimeZone.getDefault());
    	
		
		Step step1 = new Step();
		step1.setCaption("New Step 1");
		step1.setBackgroundColor("#9cfb84");
		step1.setStartDate(LocalDateTime.of(2020, 4, 7, 0, 0));
		step1.setEndDate(LocalDateTime.of(2020, 4, 11, 0, 0));
		
		Step step2 = new Step();
		step2.setCaption("New Step 2");
		step2.setBackgroundColor("#a3d9ff");
		step2.setStartDate(LocalDateTime.of(2020, 4, 7, 0, 0));
		step2.setEndDate(LocalDateTime.of(2020, 4, 11, 0, 0));
		
		gantt.addStep(step1);
		gantt.addStep(step2);
		
		add(gantt);
	}
}
