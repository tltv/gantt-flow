package org.vaadin.tltv.gantt;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.tltv.gantt.element.StepElement;
import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import elemental.json.JsonArray;
import elemental.json.impl.JreJsonFactory;

@Tag("gantt-element")
@NpmPackage(value = "tltv-gantt-element", version = "^1.0.1")
@NpmPackage(value = "tltv-timeline-element", version = "^1.0.7")
@NpmPackage(value = "date-fns", version = "^2.9.0")
@NpmPackage(value = "date-fns-tz", version = "^1.0.9")
@JsModule("tltv-gantt-element/src/gantt-element.ts")
public class Gantt extends Component implements HasSize {

	private final JreJsonFactory jsonFactory = new JreJsonFactory();

	public void setResolution(Resolution resolution) {
		getElement().setAttribute("resolution",
				Objects.requireNonNull(resolution, "Setting null Resolution is not allowed").name());
	}

	public Resolution getResolution() {
		return Resolution.valueOf(getElement().getAttribute("resolution"));
	}

	public void setLocale(Locale locale) {
		getElement().setAttribute("locale",
				Objects.requireNonNull(locale, "Setting null Locale is not allowed").toLanguageTag());
		setupByLocale();
	}

	public Locale getLocale() {
		return Locale.forLanguageTag(getElement().getAttribute("locale"));
	}

	public void setTimeZone(TimeZone timeZone) {
		getElement().setAttribute("zone",
				Objects.requireNonNull(timeZone, "Setting null TimeZone is not allowed").getID());
	}

	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone(getElement().getAttribute("zone"));
	}

	public void setStartDate(LocalDate startDate) {
		getElement().setAttribute("start", GanttUtil.formatDateTime(resetTimeToMin(startDate.atStartOfDay())));
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		getElement().setAttribute("start", GanttUtil.formatDateTime(resetTimeToMin(startDateTime)));
	}

	public LocalDateTime getStartDateTime() {
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("start")));
	}

	public void setEndDate(LocalDate endDate) {
		getElement().setAttribute("end", GanttUtil.formatDateTime(resetTimeToMin(endDate.atStartOfDay())));
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		getElement().setAttribute("end", GanttUtil.formatDateTime(resetTimeToMin(endDateTime)));
	}

	public LocalDateTime getEndDateTime() {
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("end")));
	}

	public void setYearRowVisible(boolean visible) {
		getElement().setProperty("yearRowVisible", visible);
	}

	public boolean isYearRowVisible() {
		return getElement().getProperty("yearRowVisible", true);
	}

	public void setMonthRowVisible(boolean visible) {
		getElement().setProperty("monthRowVisible", visible);
	}

	public boolean isMonthRowVisible() {
		return getElement().getProperty("monthRowVisible", true);
	}

	public void addStep(Step step) {
		getElement().appendChild(new StepElement(ensureUID(step)).getElement());
	}
	
	public void addStep(int index, Step step) {
        if (contains(ensureUID(step))) {
            moveStep(index, step);
        } else {
        	getElement().insertChild(index, new StepElement(ensureUID(step)).getElement());
        }
    }

    public void moveStep(int toIndex, Step step) {
        if (!contains(step)) {
            return;
        }
        String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
        Step moveStep = step;
        if (targetStepUid.equals(moveStep.getUid())) {
            return;
        }
        getStepElements().filter(item -> item.getUid().equals(moveStep.getUid())).findFirst().ifPresent(StepElement::removeFromParent);
        getElement().insertChild(indexOf(targetStepUid), new StepElement(moveStep).getElement());
    }
	
	private void setupByLocale() {
		setArrayProperty("monthNames", new DateFormatSymbols(getLocale()).getMonths());
		setArrayProperty("weekdayNames", new DateFormatSymbols(getLocale()).getWeekdays());
		// First day of week (1 = sunday, 2 = monday)
		final java.util.Calendar cal = new GregorianCalendar(getLocale());
		getElement().setProperty("firstDayOfWeek", cal.getFirstDayOfWeek() - 1);
	}

	private void setArrayProperty(String name, String[] array) {
		final JsonArray jsonArray = jsonFactory.createArray();
		for (int index = 0; index < array.length; index++) {
			jsonArray.set(index, array[index]);
		}
		getElement().executeJs("this." + name + " = $0;", jsonArray);
	}

	LocalDateTime resetTimeToMin(LocalDateTime dateTime) {
		return GanttUtil.resetTimeToMin(dateTime, getResolution());
	}

	LocalDateTime resetTimeToMax(LocalDateTime dateTime, boolean exclusive) {
		return GanttUtil.resetTimeToMax(dateTime, getResolution(), exclusive);
	}
	
    public Stream<StepElement> getStepElements() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast);
	}

	public boolean contains(Step targetStep) {
        return getChildren()
        		.filter(child -> child instanceof StepElement).map(StepElement.class::cast)
        		.anyMatch(step -> step.getUid().equals(targetStep.getUid()));
    }
    
	public int indexOf(Step step) {
		return indexOf(step.getUid());
	}
	
    public int indexOf(String stepUid) {
    	List<String> uidList = getStepElements().map(StepElement::getUid).collect(Collectors.toList());
        return uidList.indexOf(stepUid);
    }
    
	/**
     * Ensures that given step has UID. If not, then generates one.
     */
    protected <T extends GanttStep> T ensureUID(T step) {
        if (step == null) {
            return null;
        }
        if (step.getUid() == null || step.getUid().isEmpty()) {
            step.setUid(UUID.randomUUID().toString());
        }
        return step;
    }
}
