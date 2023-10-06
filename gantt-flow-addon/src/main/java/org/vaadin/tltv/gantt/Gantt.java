package org.vaadin.tltv.gantt;

import java.text.DateFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.tltv.gantt.element.StepElement;
import org.vaadin.tltv.gantt.event.GanttClickEvent;
import org.vaadin.tltv.gantt.event.GanttDataChangeEvent;
import org.vaadin.tltv.gantt.event.StepClickEvent;
import org.vaadin.tltv.gantt.event.StepMoveEvent;
import org.vaadin.tltv.gantt.event.StepResizeEvent;
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
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.impl.JreJsonFactory;

import static java.util.Optional.ofNullable;

@Tag("gantt-element")
@NpmPackage(value = "tltv-gantt-element", version = "1.0.27")
@NpmPackage(value = "tltv-timeline-element", version = "1.0.20")
@JsModule("tltv-gantt-element/dist/src/gantt-element.js")
@CssImport(value = "gantt-grid.css", themeFor = "vaadin-grid")
public class Gantt extends Component implements HasSize {

	private final JreJsonFactory jsonFactory = new JreJsonFactory();

	private Grid<Step> captionGrid;
	private Registration captionGridDataChangeListener;
	private Registration captionGridColumnResizeListener;
	
	public void setResolution(Resolution resolution) {
		getElement().setAttribute("resolution",
				Objects.requireNonNull(resolution, "Setting null Resolution is not allowed").name());
		refreshForHorizontalScrollbar();
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
		getElement().setAttribute("start", GanttUtil.formatDate(resetTimeToMin(startDate.atStartOfDay())));
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		getElement().setAttribute("start", GanttUtil.formatDateHour(resetTimeToMin(startDateTime)));
	}

	public LocalDate getStartDate() {
		return LocalDate.from(GanttUtil.parseDate(getElement().getAttribute("start")));
	}
	
	public LocalDateTime getStartDateTime() {
		if(getResolution() == Resolution.Hour) {
			return LocalDateTime.from(GanttUtil.parse(getElement().getAttribute("start")));
		}
		return LocalDateTime.of(getStartDate(), LocalTime.MIN);
	}

	public void setEndDate(LocalDate endDate) {
		getElement().setAttribute("end", GanttUtil.formatDate(resetTimeToMin(endDate.atStartOfDay())));
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		getElement().setAttribute("end", GanttUtil.formatDateHour(resetTimeToMin(endDateTime)));
	}

	public LocalDate getEndDate() {
		return LocalDate.from(GanttUtil.parse(getElement().getAttribute("end")));
	}
	
	public LocalDateTime getEndDateTime() {
		if(getResolution() == Resolution.Hour) {
			return LocalDateTime.from(GanttUtil.parse(getElement().getAttribute("end")));
		}
		return LocalDateTime.of(getEndDate(), LocalTime.MIN);
	}

	public boolean isTwelveHourClock() {
		return getElement().getProperty("twelveHourClock", false);
	}

	public void setTwelveHourClock(boolean enabled) {
		getElement().setProperty("twelveHourClock", enabled);
	}

	public void setYearRowVisible(boolean visible) {
		getElement().setProperty("yearRowVisible", visible);
		refreshForHorizontalScrollbar();
	}

	public boolean isYearRowVisible() {
		return getElement().getProperty("yearRowVisible", true);
	}

	public void setMonthRowVisible(boolean visible) {
		getElement().setProperty("monthRowVisible", visible);
		refreshForHorizontalScrollbar();
	}

	public boolean isMonthRowVisible() {
		return getElement().getProperty("monthRowVisible", true);
	}

	public void setMovableSteps(boolean enabled) {
		getElement().setProperty("movableSteps", enabled);
	}
	
	public boolean isMovableSteps() {
		return getElement().getProperty("movableSteps", true); 
	}
	
	public void setResizableSteps(boolean enabled) {
		getElement().setProperty("resizableSteps", enabled);
	}
	
	public boolean isResizableSteps() {
		return getElement().getProperty("resizableSteps", true); 
	}
	
	public void setMovableStepsBetweenRows(boolean enabled) {
		getElement().setProperty("movableStepsBetweenRows", enabled);
	}
	
	public boolean isMovableStepsBetweenRows() {
		return getElement().getProperty("movableStepsBetweenRows", true); 
	}
	
	public void addSteps(Collection<Step> steps) {
		if(steps != null) {
			addSteps(steps.stream());
		}
	}
	
	public void addSteps(Step... steps) {
		if(steps != null) {
			addSteps(Stream.of(steps));
		}
	}
	
	public void addSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		steps.forEach(this::appendStep);
		fireDataChangeEvent();
	}
	
	public void addStep(Step step) {
		appendStep(step);
		fireDataChangeEvent();
	}

	public void addSubStep(SubStep subStep) {
		StepElement ownerStepElement = getStepElements().collect(Collectors.toList())
				.get(indexOf(subStep.getOwner().getUid()));
		ownerStepElement.getElement().appendChild(new StepElement(ensureUID(subStep)).getElement());
	}
	
	public void addStep(int index, Step step) {
        if (contains(ensureUID(step))) {
            moveStep(index, step);
        } else {
        	getElement().insertChild(index, new StepElement(ensureUID(step)).getElement());
        	fireDataChangeEvent();
        }
    }

	public void moveStep(int toIndex, GanttStep anyStep) {
		if(anyStep.isSubstep()) {
			moveSubStep(toIndex, (SubStep) anyStep);
		} else {
			moveStep(toIndex, (Step) anyStep);
		}
	}
	
    public void moveStep(int toIndex, Step step) {
        if (!contains(step)) {
            return;
        }
        String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
        int fromIndex = indexOf(step);
        Step moveStep = step;
        if (!targetStepUid.equals(moveStep.getUid())) {
        	var subStepEements = getSubStepElements(moveStep.getUid());
        	// memorize context menu builders before removing old element with builders.
        	var contextMenuBuilders = getStepElementOptional(moveStep.getUid()).map(StepElement::getContextMenuBuilders).orElse(List.of());
			// and also tooltips.
			var tooltips =  getStepElementOptional(moveStep.getUid()).map(StepElement::getTooltips).orElse(List.of());
        	getStepElementOptional(moveStep.getUid()).ifPresent(StepElement::removeFromParent);
        	StepElement stepElement = new StepElement(moveStep);
        	subStepEements.forEach(subStepElement -> stepElement.getElement().appendChild(subStepElement.getElement()));
        	if(fromIndex <= toIndex) {
        		getElement().insertChild(indexOf(targetStepUid) + 1, stepElement.getElement());
        	} else {
        		getElement().insertChild(indexOf(targetStepUid), stepElement.getElement());
        	}
        	// add context menu builders back in the end.
			contextMenuBuilders.stream().forEach(stepElement::addContextMenu);
			// and tooltips.
			tooltips.forEach(stepElement::addTooltip); 
        	fireDataChangeEvent();
        }
        updateSubStepsByMovedOwner(moveStep.getUid());
    }
    
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
			getSubStepElements().filter(item -> item.getUid().equals(subStep.getUid())).findFirst()
					.ifPresent(StepElement::removeFromParent);
			subStep.setOwner(getStep(targetStepUid));
			substepElement = new StepElement(subStep);
			stepElement.getElement().appendChild(substepElement.getElement());
			// add context menu builders back in the end.
			contextMenuBuilders.stream().forEach(substepElement::addContextMenu);
			// and tooltips.
			tooltips.forEach(substepElement::addTooltip); 
		}
		subStep.updateOwnerDatesBySubStep();
		stepElement.refresh();
    }

	public void removeSteps(Collection<Step> steps) {
		if(steps != null) {
			removeSteps(steps.stream());
		}
	}
	
	public void removeSteps(Step... steps) {
		if(steps != null) {
			removeSteps(Stream.of(steps));
		}
	}
	
	public void removeSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		steps.forEach(this::doRemoveStep);
		fireDataChangeEvent();
	}
	
	public boolean removeAnyStep(String uid) {
		return doRemoveAnyStep(uid);
	}
	
	public boolean removeAnyStep(GanttStep step) {
		return doRemoveAnyStep(step.getUid());
	}
	
	public boolean removeStep(Step step) {
		if (step == null) {
			return false;
		}
		if(doRemoveStep(step)) {
			fireDataChangeEvent();
			return true;
		}
		return false;
	}

	private boolean doRemoveStep(Step step) {
		return doRemoveAnyStep(step.getUid());
	}
	
	private boolean doRemoveAnyStep(String uid) {
		var removedStepElement = getStepElement(uid);
		if (removedStepElement != null) {
			removedStepElement.removeFromParent();
			if (removedStepElement.getModel().isSubstep()) {
				refresh(((SubStep) removedStepElement.getModel()).getOwner().getUid());
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

	LocalDateTime resetTimeToMin(LocalDateTime dateTime) {
		return GanttUtil.resetTimeToMin(dateTime, getResolution());
	}

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
    
    public Stream<Step> getSteps() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast)
				.map(StepElement::getModel).map(Step.class::cast);
	}
    
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

    public boolean contains(String targetUid) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
    public boolean contains(GanttStep targetStep) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetStep.getUid()));
    }
    
	public boolean contains(Step targetStep) {
		return contains(getStepElements(), targetStep.getUid());
    }
	
	public boolean contains(SubStep targetSubStep) {
        return contains(getSubStepElements(), targetSubStep.getUid());
    }
	
	private boolean contains(Stream<StepElement> stream, String targetUid) {
        return stream
        		.filter(child -> child instanceof StepElement).map(StepElement.class::cast)
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
	public int indexOf(Step step) {
		return indexOf(step.getUid());
	}
	
    public int indexOf(String stepUid) {
    	GanttStep step = getAnyStep(stepUid);
    	if(step.isSubstep()) {
    		step = ((SubStep) step).getOwner();
    	}
    	List<String> uidList = getStepElements().map(StepElement::getUid).collect(Collectors.toList());
        return uidList.indexOf(step.getUid());
    }
    
    public SubStep getSubStep(String uid) {
		return getSubStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).map(SubStep.class::cast).orElse(null);
	}
    
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
		return addListener(StepMoveEvent.class, listener);
	}
	
	public Registration addStepResizeListener(ComponentEventListener<StepResizeEvent> listener) {
		return addListener(StepResizeEvent.class, listener);
	}
	
	public Registration addDataChangeListener(ComponentEventListener<GanttDataChangeEvent> listener) {
		return addListener(GanttDataChangeEvent.class, listener);
	}
	
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
	
	public void removeCaptionGrid() {
		if(captionGrid != null) {
			captionGridDataChangeListener.remove();
			captionGridColumnResizeListener.remove();
			getElement().executeJs("this.registerScrollElement(null)");
			getElement().executeJs("this._container.style.overflowX = 'auto';");
			captionGrid = null;
		}
	}
	
	public Grid<Step> getCaptionGrid() {
		return captionGrid;
	}
	
	private void refreshForHorizontalScrollbar() {
		if(captionGrid == null) {
			return;
		}
		getElement().executeJs(
                "let self = this;\n" +
                "this.updateComplete.then(() => {\n" +
                "		$0.style.setProperty('--gantt-caption-grid-header-height', self._timeline.clientHeight+'px');\n" +
                "		$0.$.table.style.width='calc(100% + '+self.scrollbarWidth+'px';\n" +
                "		const left = $0.$.table.scrollLeft > 0;\n" +
                "		const right = $0.$.table.scrollLeft < $0.$.table.scrollWidth - $0.$.table.clientWidth;\n" +
                "		const gridOverflowX = left || right;\n" +
                "		this._container.style.overflowX = (gridOverflowX) ? 'scroll' : 'auto';\n" +
                "		if(self.isContentOverflowingHorizontally() && !gridOverflowX) {\n" +
                "			$0.$.scroller.style.height = 'calc(100% - ' + self.scrollbarWidth + 'px)';\n" +
                "		} else {\n" +
                "			$0.$.scroller.style.removeProperty('height');\n" +
                "		}\n" +
                "	})\n",
				captionGrid);
	}
	
	private void fireDataChangeEvent() {
		fireEvent(new GanttDataChangeEvent(this));
	}
}
