/*
 * Copyright 2023 Tomi Virtanen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 */

package org.vaadin.tltv.gantt;

import java.text.DateFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;
import org.vaadin.tltv.gantt.element.StepElement;
import org.vaadin.tltv.gantt.event.GanttClickEvent;
import org.vaadin.tltv.gantt.event.GanttDataChangeEvent;
import org.vaadin.tltv.gantt.event.StepClickEvent;
import org.vaadin.tltv.gantt.event.StepMoveEvent;
import org.vaadin.tltv.gantt.event.StepResizeEvent;
import org.vaadin.tltv.gantt.event.GanttDataChangeEvent.DataEvent;
import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.model.SubStep;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.impl.JreJsonFactory;

import static java.util.Optional.ofNullable;

/**
 * Gantt is a component that shows Gantt chart which is by definition
 * a bar chart that illustrates a project schedule. It's a list of steps where
 * individual step can be divided in multiple substeps. Steps are movable and
 * resizable by mouse/touch devices and API gives control to
 * insert/update/remove steps programmatically. Steps are shown on a timeline
 * which has Day, Week and Hour resolutions. Resolution, date range, language
 * and timezone can be changed with the public API.
 * <p>
 * Gantt component uses 'tltv-gantt-element' and 'tltv'timeline.element' web
 * components.
 */
@Tag("gantt-element")
@NpmPackage(value = "tltv-gantt-element", version = "1.0.30")
@NpmPackage(value = "tltv-timeline-element", version = "1.0.20")
@JsModule("tltv-gantt-element/dist/src/gantt-element.js")
@CssImport(value = "gantt-grid.css", themeFor = "vaadin-grid")
public class Gantt extends Component implements HasSize {

	private final JreJsonFactory jsonFactory = new JreJsonFactory();

	private Grid<Step> captionGrid;
	private Registration captionGridDataChangeListener;
	private Registration captionGridColumnResizeListener;
	private final Set<ComponentEventListener<StepMoveEvent>> moveListeners = new HashSet<>();
	
	/**
	 * Default contructor with default settings. Sets locale to match
	 * {@link Component#getLocale()}.
	 */
	public Gantt() {
		setupDefaults();
		addListener(StepMoveEvent.class, event -> {
			// dates and position are synchronized automatically to server side model
			event.getAnyStep().setStartDate(event.getStart());
			event.getAnyStep().setEndDate(event.getEnd());
			moveStep(indexOf(event.getNewUid()), event.getAnyStep(), true);
			fireMoveListeners(event);
		});
	}

	/**
	 * Setup component with default settings.
	 */
	public void setupDefaults() {
		setResolution(Resolution.Day);
		setLocale(super.getLocale());
		setTimeZone(TimeZone.getTimeZone("Europe/London"));
		setStartDate(LocalDate.now());
		setEndDate(LocalDate.now().plusMonths(1));
	}

	/**
	 * Set new timeline resolution. Allowed resolutions are
	 * {@link Resolution#Hour}, {@link Resolution#Day} and
	 * {@link Resolution#Week}.
	 *
	 * @param resolution {@link Resolution} enum. Not null.
	 */
	public void setResolution(Resolution resolution) {
		getElement().setAttribute("resolution",
				Objects.requireNonNull(resolution, "Setting null Resolution is not allowed").name());
		refreshForHorizontalScrollbar();
	}

	/**
	 * Get current timeline resolution. Default is {@link Resolution#Day}.
	 * 
	 * @return {@link Resolution} enum
	 */
	public Resolution getResolution() {
		return Resolution.valueOf(getElement().getAttribute("resolution"));
	}

	/**
	 * Set {@link Locale}. Setting locate updates locale of the web component by
	 * language tag. It also
	 * updates month names, week day names and first day of week based on the
	 * {@link Locale}.
	 * 
	 * @param locale New {@link Locale}. Should not be null.
	 */
	public void setLocale(Locale locale) {
		getElement().setAttribute("locale",
				Objects.requireNonNull(locale, "Setting null Locale is not allowed").toLanguageTag());
		setupByLocale();
	}

	/**
	 * Return active locale based on the language tag in the web component.
	 * This may not be in sync with the application locale if it's changed with
	 * {@link Gantt#setLocale(Locale)} or if application locale is changed after
	 * constructor is called. Default constructor calls it once with application's
	 * locale read with {@link Component#getLocale()}.
	 * 
	 * @return Active {@link Locale}
	 */
	@Override
	public Locale getLocale() {
		return Locale.forLanguageTag(getElement().getAttribute("locale"));
	}

	/**
	 * Set {@link TimeZone}.
	 * 
	 * @param timeZone New {@link TimeZone}. Should not be null.
	 */
	public void setTimeZone(TimeZone timeZone) {
		getElement().setAttribute("zone",
				Objects.requireNonNull(timeZone, "Setting null TimeZone is not allowed").getID());
	}

	/**
	 * Get currently active {@link TimeZone} based on the zone in the web component.
	 * Default is "Europe/London".
	 * 
	 * @return Active {@link TimeZone}
	 */
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone(getElement().getAttribute("zone"));
	}

	/**
	 * Set start date of the timeline. Call this only with {@link Resolution#Day}
	 * and {@link Resolution#Week}.
	 * 
	 * @param startDate Inclusive {@link LocaDate}. Not null.
	 */
	public void setStartDate(LocalDate startDate) {
		Objects.requireNonNull(startDate, "Setting null start date is not allowed");
		getElement().setAttribute("start", GanttUtil.formatDate(resetTimeToMin(startDate.atStartOfDay())));
	}

	/**
	 * Set start date and time of the timeline. Call this only with
	 * {@link Resolution#Hour}.
	 * 
	 * @param startDate Inclusive {@link LocaDateTime}. Not null.
	 */
	public void setStartDateTime(LocalDateTime startDateTime) {
		Objects.requireNonNull(startDateTime, "Setting null start date time is not allowed");
		getElement().setAttribute("start", GanttUtil.formatDateHour(resetTimeToMin(startDateTime)));
	}

	/**
	 * Get start date of the timeline based on the web component's <code>start</code>
	 * attribute.
	 * 
	 * @return Inclusive {@link LocaDate}
	 */
	public LocalDate getStartDate() {
		return LocalDate.from(GanttUtil.parseDate(getElement().getAttribute("start")));
	}
	
	/**
	 * Get start datetime of the timeline based on the web component's
	 * <code>start</code> attribute.
	 * 
	 * @return Inclusive {@link LocaDateTime}
	 */
	public LocalDateTime getStartDateTime() {
		if(getResolution() == Resolution.Hour) {
			return LocalDateTime.from(GanttUtil.parse(getElement().getAttribute("start")));
		}
		return LocalDateTime.of(getStartDate(), LocalTime.MIN);
	}

	/**
	 * Set end date of the timeline. Call this only with {@link Resolution#Day}
	 * and {@link Resolution#Week}.
	 * 
	 * @param endDate Inclusive {@link LocaDate}. Not null.
	 */
	public void setEndDate(LocalDate endDate) {
		Objects.requireNonNull(endDate, "Setting null end date is not allowed");
		getElement().setAttribute("end", GanttUtil.formatDate(resetTimeToMin(endDate.atStartOfDay())));
	}

	/**
	 * Set end date and time of the timeline. Call this only with
	 * {@link Resolution#Hour}.
	 * 
	 * @param endDateTime Inclusive {@link LocaDateTime}. Not null.
	 */
	public void setEndDateTime(LocalDateTime endDateTime) {
		Objects.requireNonNull(endDateTime, "Setting null end date time is not allowed");
		getElement().setAttribute("end", GanttUtil.formatDateHour(resetTimeToMin(endDateTime)));
	}

	/**
	 * Get end date of the timeline based on the web component's <code>end</code>
	 * attribute.
	 * 
	 * @return Inclusive {@link LocaDate}
	 */
	public LocalDate getEndDate() {
		return LocalDate.from(GanttUtil.parse(getElement().getAttribute("end")));
	}
	
	/**
	 * Get end datetime of the timeline based on the web component's
	 * <code>end</code> attribute.
	 * 
	 * @return Inclusive {@link LocaDateTime}
	 */
	public LocalDateTime getEndDateTime() {
		if(getResolution() == Resolution.Hour) {
			return LocalDateTime.from(GanttUtil.parse(getElement().getAttribute("end")));
		}
		return LocalDateTime.of(getEndDate(), LocalTime.MIN);
	}

	/**
	 * Get value of twelve hour clock boolean flag based on the web component's
	 * <code>twelveHourClock</code> attribute. <code>false</code> defaults to 24
	 * hour clock. Affects time formatting in the timeline.
	 * 
	 * @return <code>true</code> when <code>twelveHourClock</code> is enabled and
	 *         false otherwise
	 */
	public boolean isTwelveHourClock() {
		return getElement().getProperty("twelveHourClock", false);
	}

	/**
	 * Set value of twelve hour clock boolean flag to the web component's
	 * <code>twelveHourClock</code> attribute.
	 * 
	 * @param enabled <code>true</code> when <code>twelveHourClock</code> is enabled
	 *                and false otherwise
	 */
	public void setTwelveHourClock(boolean enabled) {
		getElement().setProperty("twelveHourClock", enabled);
	}

	public void setYearRowVisible(boolean visible) {
		getElement().setProperty("yearRowVisible", visible);
		refreshForHorizontalScrollbar();
	}

	/**
	 * Get year row visibility flag for the timeline based on the web component's
	 * <code>yearRowVisible</code> property. Default is <code>true</code>.
	 */
	public boolean isYearRowVisible() {
		return getElement().getProperty("yearRowVisible", true);
	}

	public void setMonthRowVisible(boolean visible) {
		getElement().setProperty("monthRowVisible", visible);
		refreshForHorizontalScrollbar();
	}

	/**
	 * Get month row visibility flag for the timeline based on the web component's
	 * <code>monthRowVisible</code> property. Default is <code>true</code>.
	 */
	public boolean isMonthRowVisible() {
		return getElement().getProperty("monthRowVisible", true);
	}

	public void setMovableSteps(boolean enabled) {
		getElement().setProperty("movableSteps", enabled);
	}
	
	/**
	 * Get boolean value of the web component's <code>movableSteps</code> attribute.
	 * Attribute controls if steps are movable by user interactions using mouse or
	 * touch device. Default is <code>true</code>.
	 */
	public boolean isMovableSteps() {
		return getElement().getProperty("movableSteps", true); 
	}
	
	public void setResizableSteps(boolean enabled) {
		getElement().setProperty("resizableSteps", enabled);
	}
	
	/**
	 * Get boolean value of the web component's <code>resizableSteps</code> attribute.
	 * Attribute controls if steps are resizable by user interactions using mouse or
	 * touch device. Default is <code>true</code>.
	 */	
	public boolean isResizableSteps() {
		return getElement().getProperty("resizableSteps", true); 
	}
	
	public void setMovableStepsBetweenRows(boolean enabled) {
		getElement().setProperty("movableStepsBetweenRows", enabled);
	}
	
	/**
	 * Get boolean value of the web component's <code>movableStepsBetweenRows</code>
	 * attribute. Attribute controls if steps are movable between rows by user
	 * interactions using mouse or touch device. Default is <code>true</code>. If
	 * {@link #isMovableSteps()} is <code>false</code>, then steps are not movable
	 * between rows either even if set to <code>true</code>.
	 */
	public boolean isMovableStepsBetweenRows() {
		return getElement().getProperty("movableStepsBetweenRows", true); 
	}
	
	/**
	 * Add new step components based on the given collection of step descriptors.
	 * New components are appended at the end.
	 * 
	 * @param steps a collection of step descriptor objects for the new components.
	 */
	public void addSteps(Collection<Step> steps) {
		if(steps != null) {
			addSteps(steps.stream());
		}
	}
	
	/**
	 * Add new step components based on the given varargs of step descriptors. New
	 * components are appended at the end.
	 * 
	 * @param steps a varargs of step descriptor objects for the new components.
	 */
	public void addSteps(Step... steps) {
		if(steps != null) {
			addSteps(Stream.of(steps));
		}
	}
	
	/**
	 * Add new step components based on the given stream of step descriptors. New
	 * components are appended at the end.
	 * 
	 * @param steps a stream of step descriptor objects for the new components.
	 */
	public void addSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		var list = steps.toList();
		steps.forEach(this::appendStep);
		fireDataChangeEvent(DataEvent.STEP_ADD, list.stream());
	}
	
	/**
	 * Add new step component based on the given step descriptor. New component is
	 * appended at the end.
	 * 
	 * @param step a step descriptor object for the new component
	 */
	public void addStep(Step step) {
		appendStep(step);
		fireDataChangeEvent(DataEvent.STEP_ADD, Stream.of(step));
	}

	/**
	 * Add new sub step component based on the given sub step descriptor. New
	 * components are appended at the end of the owner step component layout.
	 * 
	 * @param subStep a sub step descriptor object for the new component
	 */
	public void addSubStep(SubStep subStep) {
		StepElement ownerStepElement = getStepElements().collect(Collectors.toList())
				.get(indexOf(subStep.getOwner().getUid()));
		ownerStepElement.getElement().appendChild(new StepElement(ensureUID(subStep)).getElement());
	}

	private void addSubStepElement(StepElement subStepElement) {
		StepElement ownerStepElement = getStepElements().collect(Collectors.toList())
				.get(indexOf(((SubStep) subStepElement.getModel()).getOwner().getUid()));
		ownerStepElement.getElement().appendChild(subStepElement.getElement());
	}
	
	/**
	 * Add step component based on the given step descriptor. New component is moved
	 * to the given index, moving previous component one index forward. If step
	 * already exists based on its UID, then it will be moved. See
	 * {@link #moveStep(int, GanttStep)}.
	 * 
	 * @param index zero based index for new position
	 * @param step  a step descriptor object for the new or existing component
	 */
	public void addStep(int index, Step step) {
		addStep(index, step, true);
	}

	private void addStep(int index, Step step, boolean fireDataEvent) {
        if (contains(ensureUID(step))) {
            moveStep(index, step);
        } else {
        	getElement().insertChild(index, new StepElement(ensureUID(step)).getElement());
			if (fireDataEvent) {
				fireDataChangeEvent(DataEvent.STEP_ADD, Stream.of(step));
			}
        }
    }

	/**
	 * Moves given step or sub step. Same as {@link #moveStep(int, Step)} or
	 * {@link #moveSubStep(int, SubStep)} depending on which type of a component is
	 * being moved based on the UID for the given {@link GanttStep}.
	 * 
	 * @param toIndex Target zero based index where to move the step
	 * @param anyStep step or sub step descriptor of the moved step
	 */
	public void moveStep(int toIndex, GanttStep anyStep) {
		moveStep(toIndex, anyStep, false);
	}

	private void moveStep(int toIndex, GanttStep anyStep, boolean fromClient) {
		if(anyStep.isSubstep()) {
			moveSubStep(toIndex, (SubStep) anyStep);
		} else {
			moveStep(toIndex, (Step) anyStep, fromClient);
		}
	}
	
	/**
	 * Move given existing step to the given index. Index is based on the state at
	 * the moment when method is called. Moved step component is removed and
	 * discarded and new step component takes the new position. Existing substep
	 * components are moved to the new step component. Context menus and tooltips
	 * are recreated for the new step component.
	 * 
	 * @param toIndex Target zero based index where to move the step
	 * @param step    step descriptor of the moved step
	 */
    public void moveStep(int toIndex, Step step) {
		moveStep(toIndex, step, false);
	}

	private void moveStep(int toIndex, Step step, boolean fromClient) {
        if (!contains(step)) {
            return;
        }
        String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
        int fromIndex = indexOf(step);
        Step moveStep = step;
        if (!targetStepUid.equals(moveStep.getUid())) {

			if (getCaptionTreeGrid() != null) {
				List<Step> flatSubTree = getFlatSubTreeRecursively(getCaptionTreeGrid().getTreeData(), step);
				if (flatSubTree.contains(getStep(targetStepUid))) {
					// reset to old position
					doMoveStep(indexOf(moveStep.getUid()), moveStep.getUid(), moveStep);
					updateSubStepsByMovedOwner(moveStep.getUid());
					return;
				}
			}

			doMoveStep(fromIndex, targetStepUid, moveStep);
			if(fromClient) {
        		fireDataChangeEvent(DataEvent.STEP_MOVE, Stream.of(step));
			}
        }
        updateSubStepsByMovedOwner(moveStep.getUid());
    }
    
	private void doMoveStep(int fromIndex, String targetStepUid, Step moveStep) {
		var toIndex = indexOf(targetStepUid);
		var subStepEements = getSubStepElements(moveStep.getUid());
		// memorize context menu builders before removing old element with builders.
		var contextMenuBuilders = getStepElementOptional(moveStep.getUid()).map(StepElement::getContextMenuBuilders).orElse(List.of());
		// and also tooltips.
		var tooltips = getStepElementOptional(moveStep.getUid()).map(StepElement::getTooltips).orElse(List.of());
		var components = getStepElementOptional(moveStep.getUid()).map(StepElement::getChildren)
				.orElse(Stream.empty()).toList();
		getStepElementOptional(moveStep.getUid()).ifPresent(StepElement::removeFromParent);
		StepElement stepElement = new StepElement(moveStep);
		subStepEements.forEach(subStepElement -> stepElement.getElement().appendChild(subStepElement.getElement()));
		if(targetStepUid.equals(moveStep.getUid())) {
			getElement().insertChild(toIndex, stepElement.getElement());
		} else if(fromIndex <= toIndex) {
			getElement().insertChild(indexOf(targetStepUid) + 1, stepElement.getElement());
		} else {
			getElement().insertChild(indexOf(targetStepUid), stepElement.getElement());
		}
		// add context menu builders back in the end.
		contextMenuBuilders.stream().forEach(stepElement::addContextMenu);
		// and tooltips.
		tooltips.forEach(stepElement::addTooltip);
		stepElement.add(components);
	}

	/**
	 * Move given existing substep to the given index. Index is based on the state
	 * at the moment when method is called. Moved substep component is removed and
	 * discarded and new substep component takes the new position as a new children
	 * inside the step component at the new index. {@link SubStep#getOwner()} is
	 * changed to the new owner, and step dates are adjusted to include the substep
	 * inside it. Other substep component are not touched. Context menus and tooltips
	 * are recreated for the new substep component.
	 * 
	 * @param toIndex Target zero based index where to move the substep
	 * @param subStep substep descriptor of the moved substep
	 */	
	public void moveSubStep(int toIndex, SubStep subStep) {
		if (!contains(subStep)) {
			return;
		}
		String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
		StepElement stepElement = getStepElement(targetStepUid);
		Step moveStep = subStep.getOwner();
		if (!targetStepUid.equals(moveStep.getUid())) {
			var substepElement = getSubStepElements().filter(item -> item.getUid().equals(subStep.getUid())).findFirst().orElse(null);
			// memorize context menu builders before removing old element with builders.
			var contextMenuBuilders = ofNullable(substepElement).map(StepElement::getContextMenuBuilders).orElse(List.of());
			// and also tooltips.
			var tooltips = ofNullable(substepElement).map(StepElement::getTooltips).orElse(List.of());
			var components = ofNullable(substepElement).map(StepElement::getChildren)
					.orElse(Stream.empty()).toList();
			getSubStepElements().filter(item -> item.getUid().equals(subStep.getUid())).findFirst()
					.ifPresent(StepElement::removeFromParent);
			subStep.setOwner(getStep(targetStepUid));
			substepElement = new StepElement(subStep);
			stepElement.getElement().appendChild(substepElement.getElement());
			// add context menu builders back in the end.
			contextMenuBuilders.stream().forEach(substepElement::addContextMenu);
			// and tooltips.
			tooltips.forEach(substepElement::addTooltip);
			substepElement.add(components);
		}
		subStep.updateOwnerDatesBySubStep();
		stepElement.refresh();
    }

	/**
	 * Removes all given steps if they exists, based on their UIDs.
	 * @param steps collection of steps, null does nothing
	 */
	public void removeSteps(Collection<Step> steps) {
		if(steps != null) {
			removeSteps(steps.stream());
		}
	}
	
	/**
	 * Removes all given steps if they exists, based on their UIDs.
	 * 
	 * @param steps a varargs of steps, null does nothing
	 */
	public void removeSteps(Step... steps) {
		if(steps != null) {
			removeSteps(Stream.of(steps));
		}
	}
	
	/**
	 * Removes all given steps looped through the given Stream if they exists, based
	 * on their UIDs.
	 * 
	 * @param steps Stream of steps, null does nothing
	 */
	public void removeSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		var list = steps.toList();
		list.forEach(step -> doRemoveStep(step, true));
	}
	
	/**
	 * Removes Step or sub step if it exists, based on the given UID.
	 * 
	 * @param uid Target UID for step or sub step to remove
	 * @return boolean true if step or sub step was removed, false otherwise
	 */	
	public boolean removeAnyStep(String uid) {
		return doRemoveAnyStep(uid, true);
	}
	
	/**
	 * Removes Step or sub step if it exists, based on its UID.
	 * 
	 * @param step Target step or sub step to remove
	 * @return boolean true if step or sub step was removed, false otherwise
	 */
	public boolean removeAnyStep(GanttStep step) {
		return doRemoveAnyStep(step.getUid(), true);
	}
	
	/**
	 * Removes given Step if it exists, based on its UID.
	 * 
	 * @param step Target step to remove
	 * @return boolean true if step was removed, false otherwise
	 */
	public boolean removeStep(Step step) {
		return doRemoveStep(step, true);
	}

	private boolean doRemoveStep(Step step, boolean fireDataEvent) {
		return doRemoveAnyStep(step.getUid(), fireDataEvent);
	}
	
	private boolean doRemoveAnyStep(String uid, boolean fireDataEvent) {
		var removedStepElement = getStepElement(uid);
		if (removedStepElement != null) {
			removedStepElement.removeFromParent();
			if (removedStepElement.getModel().isSubstep()) {
				refresh(((SubStep) removedStepElement.getModel()).getOwner().getUid());
			} else if(fireDataEvent) {
				fireDataChangeEvent(DataEvent.STEP_REMOVE, Stream.of((Step) removedStepElement.getModel()));
			}
			return true;
		}
		return false;
	}

	private void appendStep(Step step) {
		getElement().appendChild(new StepElement(ensureUID(step)).getElement());
	}

	private void setupByLocale() {
		setArrayProperty("monthNames", new DateFormatSymbols(getLocale()).getMonths());
		setArrayProperty("weekdayNames", new DateFormatSymbols(getLocale()).getWeekdays());
		
		// First day of week (1 = sunday, 2 = monday)
		final java.util.Calendar cal = new GregorianCalendar(getLocale());
		getElement().setProperty("firstDayOfWeek", cal.getFirstDayOfWeek());
	}

	private void setArrayProperty(String name, String[] array) {
		final JsonArray jsonArray = jsonFactory.createArray();
		for (int index = 0; index < array.length; index++) {
			jsonArray.set(index, array[index]);
		}
		getElement().executeJs("this." + name + " = $0;", jsonArray);
	}

	/**
	 * Reset given datetime to timeline minimum for the given resolution.
	 * 
	 * @param dateTime Target datetime
	 * @return Truncated {@link LocalDateTime} or null with a null date.
	 * @see {@link GanttUtil#resetTimeToMin(LocalDateTime, Resolution)}
	 */
	LocalDateTime resetTimeToMin(LocalDateTime dateTime) {
		return GanttUtil.resetTimeToMin(dateTime, getResolution());
	}

	/**
	 * Reset given datetime to timeline maximum for the given resolution.
	 * 
	 * @param dateTime Target datetime
	 * @return Truncated {@link LocalDateTime} or null with a null date.
	 * @see {@link GanttUtil#resetTimeToMax(LocalDateTime, Resolution)}
	 */
	LocalDateTime resetTimeToMax(LocalDateTime dateTime, boolean exclusive) {
		return GanttUtil.resetTimeToMax(dateTime, getResolution(), exclusive);
	}
	
	/**
	 * Returns {@link StepElement} by <code>uid</code>. Includes sub-steps.
	 */
	public StepElement getStepElement(String uid) {
		return getStepElementOptional(uid).orElse(null);
	}
	
	/**
	 * Returns {@link StepElement} wrapped in {@link Optional} by <code>uid</code>. Includes sub-steps.
	 */
	public Optional<StepElement> getStepElementOptional(String uid) {
		return getFlatStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst();
	}
	
	/**
	 * Returns {@link StepElement} stream excluding sub-steps.
	 */
    public Stream<StepElement> getStepElements() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast);
	}
    
	/**
	 * Returns {@link Step} stream excluding sub-steps.
	 */
    public Stream<Step> getSteps() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast)
				.map(StepElement::getModel).map(Step.class::cast);
	}
    
	/**
	 * Returns a list of {@link Step} objects excluding sub-steps.
	 */
    public List<Step> getStepsList() {
    	return getSteps().collect(Collectors.toList());
    }
    
    /**
     * Returns {@link StepElement} stream of all steps including sub steps. 
     */
	public Stream<StepElement> getFlatStepElements() {
		Stream.Builder<StepElement> streamBuilder = Stream.builder();
		getStepElements().forEach(step -> {
			streamBuilder.add(step);
			step.getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast)
					.forEach(streamBuilder::add);
		});
		return streamBuilder.build();
	}
    
	/**
	 * Returns {@link StepElement} stream for the given step UID.
	 * @param forStepUid Target step UID
	 * @return {@link StepElement} {@link Stream}
	 */
	public Stream<StepElement> getSubStepElements(String forStepUid) {
		StepElement stepEl = getStepElement(forStepUid);
		if (stepEl != null) {
			return stepEl.getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast);
		}
		return Stream.empty();
	}
	
	/**
	 * Returns {@link StepElement} stream of all sub-steps.
	 */
    public Stream<StepElement> getSubStepElements() {
		return getStepElements().flatMap(step -> step.getChildren().filter(child -> child instanceof StepElement))
				.map(StepElement.class::cast);
	}

	/**
	 * Returns {@link SubStep} stream of all sub-steps.
	 */
	public Stream<SubStep> getSubSteps() {
		return getSubStepElements().map(StepElement::getModel).map(SubStep.class::cast);
	}

	/**
	 * Returns true if given UID matches with any existing step including sub step.
	 * 
	 * @param targetUid Target UID
	 * @return boolean true if UID exists
	 */
    public boolean contains(String targetUid) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
	/**
	 * Returns true if given {@link GanttStep}'s UID matches with any existing step
	 * including sub step.
	 * 
	 * @param targetStep Target step or sub step
	 * @return boolean true if UID exists
	 */
    public boolean contains(GanttStep targetStep) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetStep.getUid()));
    }
    
	/**
	 * Returns true if given {@link Step}'s UID matches with any existing step
	 * excluding sub step.
	 * 
	 * @param targetStep Target step
	 * @return boolean true if step with the UID exists
	 */
	public boolean contains(Step targetStep) {
		return contains(getStepElements(), targetStep.getUid());
    }
	
	/**
	 * Returns true if given {@link SubStep}'s UID matches with any existing sub step.
	 * 
	 * @param targetSubStep Target sub step
	 * @return boolean true if sub step with the UID exists
	 */	
	public boolean contains(SubStep targetSubStep) {
        return contains(getSubStepElements(), targetSubStep.getUid());
    }
	
	private boolean contains(Stream<StepElement> stream, String targetUid) {
        return stream
        		.filter(child -> child instanceof StepElement).map(StepElement.class::cast)
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
	/**
	 * Returns a zero based index of the given {@link Step}.
	 * 
	 * @param step Target {@link Step}
	 * @return zero based index
	 */
	public int indexOf(Step step) {
		return indexOf(step.getUid());
	}
	
	/**
	 * Returns a zero based index of the given UID for a step or sub step. For sub
	 * steps index is based on the owner step.
	 * 
	 * @param stepUid Target UID
	 * @return zero based index
	 */
    public int indexOf(String stepUid) {
    	GanttStep step = getAnyStep(stepUid);
    	if(step.isSubstep()) {
    		step = ((SubStep) step).getOwner();
    	}
    	List<String> uidList = getStepElements().map(StepElement::getUid).collect(Collectors.toList());
        return uidList.indexOf(step.getUid());
    }
    
	/**
	 * Returns {@link SubStep} by the UID or null if it doesn't exist.
	 * 
	 * @param uid Target UID
	 * @return {@link SubStep} or null
	 */
    public SubStep getSubStep(String uid) {
		return getSubStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).map(SubStep.class::cast).orElse(null);
	}
    
	/**
	 * Returns {@link Step} by the UID or null if it doesn't exist.
	 * 
	 * @param uid Target UID
	 * @return {@link Step} or null
	 */	
	public Step getStep(String uid) {
		return getStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).map(Step.class::cast).orElse(null);
	}
    
	/**
	 * Returns {@link GanttStep} by <code>uid</code>. Including sub-steps.
	 */
    public GanttStep getAnyStep(String uid) {
    	return getFlatStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).orElse(null);
    }

	/**
	 * Updates sub step start and end dates for moved owner step.
	 * 
	 * @param stepUid Target owner step UID
	 */
    public void updateSubStepsByMovedOwner(String stepUid) {
    	Step step = getStep(stepUid);
		// update sub-steps by moved owner
		LocalDateTime previousStart = getSubStepElements(stepUid).map(StepElement::getModel)
				.map(GanttStep::getStartDate).min(Comparator.naturalOrder()).orElse(step.getStartDate());
		Duration delta = Duration.between(previousStart, step.getStartDate());
		getSubStepElements(stepUid).forEach(substep -> {
			substep.getModel().setStartDate(substep.getModel().getStartDate().plus(delta));
			substep.getModel().setEndDate(substep.getModel().getEndDate().plus(delta));
			substep.refresh();
		});
    }
    
	/**
	 * Refresh target step element if it exists.
	 * 
	 * @param uid Target step UID
	 */
	public void refresh(String uid) {
		var stepElement = getStepElement(uid);
		if (stepElement != null) {
			stepElement.refresh();
		}
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
    
    @Override
    public void setWidth(String width) {
    	getElement().getStyle().set("--gantt-element-width", Objects.requireNonNullElse(width, "auto"));
    	getElement().callJsFunction("updateSize");
    }
    
    @Override
    public void setHeight(String height) {
    	getElement().getStyle().set("--gantt-element-height", Objects.requireNonNullElse(height, "auto"));
    	getElement().callJsFunction("updateSize");
    }
    
	public Registration addGanttClickListener(ComponentEventListener<GanttClickEvent> listener) {
		return addListener(GanttClickEvent.class, listener);
	}
	
	public Registration addStepClickListener(ComponentEventListener<StepClickEvent> listener) {
		return addListener(StepClickEvent.class, listener);
	}
	
	public Registration addStepMoveListener(ComponentEventListener<StepMoveEvent> listener) {
		moveListeners.add(listener);
		return new Registration() {
			@Override
			public void remove() {
				moveListeners.remove(listener);
			}
		};
	}

	private void fireMoveListeners(StepMoveEvent event) {
		moveListeners.forEach(listener -> listener.onComponentEvent(event));
	}
	
	public Registration addStepResizeListener(ComponentEventListener<StepResizeEvent> listener) {
		return addListener(StepResizeEvent.class, listener);
	}
	
	public Registration addDataChangeListener(ComponentEventListener<GanttDataChangeEvent> listener) {
		return addListener(GanttDataChangeEvent.class, listener);
	}
	
	/**
	 * Builds a new {@link Grid} instance with a single column that renders text
	 * based on the step caption. {@link Grid} will be kept in sync with the Gantt
	 * steps. Instance is available then with {@link #getCaptionGrid()}. This does
	 * not attach component to any layout.
	 * 
	 * @param header Header of the column
	 * @return A new {@link Grid} instance
	 */
	public Grid<Step> buildCaptionGrid(String header) {
		removeCaptionGrid();
		var grid = new Grid<Step>();
		this.captionGrid = grid;
		grid.getStyle().set("--gantt-caption-grid-row-height", "30px");
		grid.addClassName("gantt-caption-grid");
		grid.addColumn(LitRenderer.<Step>of("<span>${item.caption}</span>").withProperty("caption", Step::getCaption))
				.setHeader(header).setResizable(true);
		captionGridColumnResizeListener = grid.addColumnResizeListener(event -> {
			if(event.isFromClient()) {
				refreshForHorizontalScrollbar();
			}
		});
		grid.setItems(query -> getSteps().skip(query.getOffset()).limit(query.getLimit()));
		captionGridDataChangeListener = addDataChangeListener(event -> {
			grid.getLazyDataView().refreshAll();
			refreshForHorizontalScrollbar();
		});
		getElement().executeJs("this.registerScrollElement($0.$.table)", grid);
		refreshForHorizontalScrollbar();
		return grid;
	}

	/**
	 * Builds a new {@link TreeGrid} instance with a single column that renders text
	 * based on the step caption. {@link TreeGrid} will be kept in sync with the Gantt
	 * steps. Instance is available then with {@link #getCaptionGrid()}. This does
	 * not attach component to any layout. 
	 * 
	 * @param header Header of the column
	 * @return A new {@link TreeGrid} instance
	 */
	public TreeGrid<Step> buildCaptionTreeGrid(String header) {
		removeCaptionGrid();
		var grid = new TreeGrid<Step>();
		this.captionGrid = grid;
		grid.getStyle().set("--gantt-caption-grid-row-height", "30px");
		grid.addClassName("gantt-caption-grid");
		grid.addHierarchyColumn(Step::getCaption)
				.setHeader(header).setResizable(true).setSortable(false);
		captionGridColumnResizeListener = grid.addColumnResizeListener(event -> {
			if (event.isFromClient()) {
				refreshForHorizontalScrollbar();
			}
		});
		TreeData<Step> treeData = new TreeData<>();
		treeData.addRootItems(getStepsList());
		var dataProvider = new TreeDataProvider<>(treeData);
		grid.setDataProvider(dataProvider);
		grid.addExpandListener(event -> {
			addChildStepRecursively(grid, event.getItems(), new MutableInt());
		});
		grid.addCollapseListener(event -> {
			removeChildStepRecursively(grid, event.getItems());
		});
		captionGridDataChangeListener = addDataChangeListener(event -> {
			switch (event.getDataEvent()) {
				case STEP_ADD:
					event.getSteps().forEach(step -> handleTreeDataAdd(treeData, step));
					break;
				case STEP_REMOVE:
					event.getSteps().forEach(step -> {
						removeChildStepRecursively(getCaptionTreeGrid(), step);
						grid.getTreeData().removeItem(step);
					});
					break;
				case STEP_MOVE:
					event.getSteps().forEach(step -> handleTreeDataMove(grid.getTreeData(), step));
					break;
				default:
					break;
			}
			grid.getDataProvider().refreshAll();
			refreshForHorizontalScrollbar();
		});
		getElement().executeJs("this.registerScrollElement($0.$.table)", grid);
		refreshForHorizontalScrollbar();
		return grid;
	}
	
	protected void handleTreeDataAdd(TreeData<Step> treeData, Step step) {
		treeData.addRootItems(step);
		int flatSiblingIndex = indexOf(step) - 1;
		if (flatSiblingIndex >= 0) {
			Step flatSibling = getStepsList().get(flatSiblingIndex);
			if (getCaptionTreeGrid().isExpanded(flatSibling)) {
				treeData.setParent(step, flatSibling);
				treeData.moveAfterSibling(step, null);
			} else {
				treeData.setParent(step, treeData.getParent(flatSibling));
				treeData.moveAfterSibling(step, flatSibling);
			}
		} else {
			treeData.moveAfterSibling(step, null);
		}
	}

	protected void handleTreeDataMove(TreeData<Step> treeData, Step step) {
		Step oldParent = treeData.getParent(step);
		Step newParent = null;
		int index = indexOf(step);
		Step sibling = null;
		if (index > 0) {
			List<Step> flatSubTree = getFlatSubTreeRecursively(treeData, step);
			sibling = getStepsList().get(index - 1);
			if (!flatSubTree.contains(sibling)) {
				if (Objects.equals(sibling, oldParent)) {
					newParent = sibling;
					sibling = null;
				} else {
					newParent = treeData.getParent(sibling);
				}
				treeData.setParent(step, newParent);
				treeData.moveAfterSibling(step, sibling);
			} else {
				sibling = null;
			}
		} else {
			treeData.setParent(step, newParent);
			treeData.moveAfterSibling(step, null);
		}
		// This is a messy way to sync state tree for updated DOM tree.
		// First it updates the state tree in correct order by removing elements and adding elements, 
		// then it removes all steps and adds them back to avoid processing previous changes in incorrect order in client side.
		boolean isStepExpanded = getCaptionTreeGrid().isExpanded(step);
		boolean isSiblingStepExpanded = sibling != null && getCaptionTreeGrid().isExpanded(sibling);
		if(isStepExpanded) {
			removeChildStepRecursively(getCaptionTreeGrid(), step);
		}
		if(isSiblingStepExpanded) {
			removeChildStepRecursively(getCaptionTreeGrid(), sibling);
		}
		if(isStepExpanded) {
			expand(step);
		}
		if(isSiblingStepExpanded) {
			expand(sibling);
		}
		if(isStepExpanded) {
			// state tree changes are messed up now. DOM is correct. Need to reset it all.
			reset();
		}
	}

	private void reset() {
		var allSteps = getStepElements().toList();
		var allSubSteps = getSubStepElements().toList();
		allSteps.forEach(s -> doRemoveStep((Step) s.getModel(), false));
		allSteps.forEach(s -> getElement().appendChild(s.getElement()));
		allSubSteps.forEach(this::addSubStepElement);
	}
	/**
	 * Expands all child steps directed by the caption TreeGrid's hierarchical data source.
	 */
	public void expand(Step item) {
		expand(List.of(item));
	}

	/**
	 * Expands all child steps directed by the caption TreeGrid's hierarchical data source.
	 */
	public void expand(Collection<Step> items) {
		if(getCaptionTreeGrid() == null) {
			return;
		}
		addChildStepRecursively(getCaptionTreeGrid(), items, new MutableInt());
	}

	private void addChildStepRecursively(TreeGrid<Step> grid, Collection<Step> items, MutableInt index) {
		items.forEach(item -> addChildStepRecursively(grid, item, index));
	}

	private void addChildStepRecursively(TreeGrid<Step> grid, Step item, MutableInt index) {
		var dataProvider = grid.getDataProvider();
		if (!dataProvider.hasChildren(item)) {
			return;
		}
		if (index.getValue() == 0) {
			index.setValue(getStepsList().indexOf(item) + 1);
		}
		for (Step child : grid.getTreeData().getChildren(item)) {
			addStep(index.getValue(), child, false);
			index.increment();
			addChildStepRecursively(grid, child, index);
		}
	}

	private List<Step> getFlatSubTreeRecursively(TreeData<Step> treeData, Step step) {
		List<Step> steps = new ArrayList<>();
		if (treeData.contains(step)) {
			for (Step child : treeData.getChildren(step)) {
				steps.add(child);
				steps.addAll(getFlatSubTreeRecursively(treeData, child));
			}
		}
		return steps;
	}

	/**
	 * Remove all child steps directed by the TreeGrid's hierarchical data
	 * source.
	 */
	private void removeChildStepRecursively(TreeGrid<Step> grid, Collection<Step> items) {
		items.stream().forEach(item -> removeChildStepRecursively(grid, item));
	}

	/**
	 * Remove all child steps directed by the TreeGrid's hierarchical data
	 * source.
	 */
	private void removeChildStepRecursively(TreeGrid<Step> grid, Step step) {
		var dataProvider = grid.getDataProvider();
		if (dataProvider.hasChildren(step)) {
			for (Step child : grid.getTreeData().getChildren(step)) {
				doRemoveStep(child, false);
				removeChildStepRecursively(grid, child);
			}
		}
	}

	/**
	 * Remove caption {@link Grid} instance if it exists. Grid will not be kept in
	 * sync with the Gantt after calling this. This does not detach component from
	 * the layout. Synchronized Grid can be created with
	 * {@link #buildCaptionGrid(String)} or {@link #buildCaptionTreeGrid(String)}.
	 */
	public void removeCaptionGrid() {
		if(captionGrid != null) {
			captionGridDataChangeListener.remove();
			captionGridColumnResizeListener.remove();
			getElement().executeJs("this.registerScrollElement(null)");
			getElement().executeJs("this._container.style.overflowX = 'auto';");
			captionGrid = null;
		}
	}
	
	/**
	 * Get caption {@link Grid} instance or null if it's not set. See
	 * {@link #buildCaptionGrid(String)} and {@link #buildCaptionTreeGrid(String)}.
	 * 
	 * @return {@link Grid} instance or null
	 */
	public Grid<Step> getCaptionGrid() {
		return captionGrid;
	}

	/**
	 * Get caption {@link TreeGrid} instance or null if it's not set. See
	 * {@link #buildCaptionTreeGrid(String)}.
	 * 
	 * @return {@link Grid} instance or null
	 */
	public TreeGrid<Step> getCaptionTreeGrid() {
		return captionGrid instanceof TreeGrid ? (TreeGrid<Step>) captionGrid : null;
	}
	
	private void refreshForHorizontalScrollbar() {
		if(captionGrid == null) {
			return;
		}
		getElement().executeJs(
				"""
				let self = this; 
				this.updateComplete.then(() => {
						$0.style.setProperty('--gantt-caption-grid-header-height', self._timeline.clientHeight+'px');
						$0.$.table.style.width='calc(100% + '+self.scrollbarWidth+'px'; 
						const left = $0.$.table.scrollLeft > 0; 
						const right = $0.$.table.scrollLeft < $0.$.table.scrollWidth - $0.$.table.clientWidth; 
						const gridOverflowX = left || right; 
						this._container.style.overflowX = (gridOverflowX) ? 'scroll' : 'auto'; 
						if(self.isContentOverflowingHorizontally() && !gridOverflowX) { 
							$0.$.scroller.style.height = 'calc(100% - ' + self.scrollbarWidth + 'px)';
							$0.$.scroller.style.minHeight = $0.$.scroller.style.height;
						} else { 
							$0.$.scroller.style.removeProperty('height');
							$0.$.scroller.style.removeProperty('min-height'); 
						}
					})
				""",
				captionGrid);
	}
	
	private void fireDataChangeEvent(DataEvent eventType, Stream<Step> steps) {
		fireEvent(new GanttDataChangeEvent(this, eventType, steps));
	}
	
}
