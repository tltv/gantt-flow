package org.vaadin.tltv.gantt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.tltv.gantt.element.StepElement;
import org.vaadin.tltv.gantt.event.GanttClickEvent;
import org.vaadin.tltv.gantt.event.StepClickEvent;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.model.SubStep;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.dom.Style.Display;
import com.vaadin.flow.dom.Style.Position;
import com.vaadin.flow.router.Route;

@Route("")
public class GanttDemoView extends VerticalLayout {

	private Gantt gantt;
	private FlexLayout scrollWrapper;
	private Grid<Step> grid;
	
	private DatePicker startDateField;
	private DatePicker endDateField;
	private TimePicker startTimeField;
	private TimePicker endTimeField;
	
	private SizeOption size = SizeOption.FULL_WIDTH;
	private int clickedBackgroundIndex;
	private LocalDateTime clickedBackgroundDate;
	private int stepCounter = 2;

	public GanttDemoView() {
		setWidthFull();
		setPadding(false);
		
		gantt = createGantt();
		gantt.setWidth("70%");
		buildCaptionGrid();
				
		Div controlPanel = buildControlPanel();

		scrollWrapper = new FlexLayout();
    	scrollWrapper.setId("scroll-wrapper");
    	scrollWrapper.setMinHeight("0");
    	scrollWrapper.setWidthFull();
    	scrollWrapper.add(
    			grid, 
    			gantt);
    	
        add(controlPanel, scrollWrapper);
	}

	private void buildCaptionGrid() {
		grid = gantt.buildCaptionGrid("Header");
		grid.setWidth("30%");
		grid.setAllRowsVisible(true);
		grid.getStyle().set("--gantt-caption-grid-row-height", "40px");
	}

	private Gantt createGantt() {
		Gantt gantt = new Gantt();
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
		
		SubStep subStepA = new SubStep(step2);
		subStepA.setCaption("R");
		subStepA.setBackgroundColor("red");
		subStepA.setStartDate(LocalDateTime.of(2020, 4, 7, 0, 0));
		subStepA.setEndDate(LocalDateTime.of(2020, 4, 8, 0, 0));
		SubStep subStepB = new SubStep(step2);
		subStepB.setCaption("G");
		subStepB.setBackgroundColor("lightgreen");
		subStepB.setStartDate(LocalDateTime.of(2020, 4, 8, 0, 0));
		subStepB.setEndDate(LocalDateTime.of(2020, 4, 9, 0, 0));
		SubStep subStepC = new SubStep(step2);
		subStepC.setCaption("B");
		subStepC.setBackgroundColor("lightblue");
		subStepC.setStartDate(LocalDateTime.of(2020, 4, 9, 0, 0));
		subStepC.setEndDate(LocalDateTime.of(2020, 4, 11, 0, 0));

		gantt.addStep(step1);
		gantt.addStep(step2);
		
		gantt.addSubStep(subStepA);
		gantt.addSubStep(subStepB);
		gantt.addSubStep(subStepC);
		
		gantt.addGanttClickListener(this::onGanttBackgroundClick);
		gantt.addStepClickListener(this::onGanttStepClick);
		gantt.addStepMoveListener(event -> {
			Notification.show("Moved step : " + event.getAnyStep().getCaption());
			
			// dates and position are not synchronized automatically to server side model
			event.getAnyStep().setStartDate(event.getStart());
			event.getAnyStep().setEndDate(event.getEnd());
			
			gantt.moveStep(gantt.indexOf(event.getNewUid()), event.getAnyStep());
		});
		gantt.addStepResizeListener(event -> {
			Notification.show("Resized step : " + event.getAnyStep().getCaption());
			
			event.getAnyStep().setStartDate(event.getStart());
			event.getAnyStep().setEndDate(event.getEnd());
			
			if(event.getAnyStep().isSubstep()) {
				((SubStep) event.getAnyStep()).updateOwnerDatesBySubStep();
				event.getSource().refresh(((SubStep) event.getAnyStep()).getOwner().getUid());
			}
		});
		
		// Add tooltip for step1 
		gantt.getStepElement(step1.getUid()).addTooltip("Tooltip for " + step1.getCaption());
		// and sub step A
		gantt.getStepElement(subStepA.getUid()).addTooltip("Tooltip for " + subStepA.getCaption());

		// Add progress bar for step1
		gantt.getStepElement(step1.getUid()).add(createProgressBar(30));

		// Add dynamic context menu for gantt background. Clicked index is registered via addGanttClickListener and addStepClickListener.
		addDynamicBackgroundContextMenu(gantt);
        
		// Add dynamic context menu for sub steps
		addDynamicSubStepContextMenu(gantt.getStepElement(subStepA.getUid()));
		addDynamicSubStepContextMenu(gantt.getStepElement(subStepB.getUid()));
		addDynamicSubStepContextMenu(gantt.getStepElement(subStepC.getUid()));
		
		return gantt;
	}

	private ProgressBar createProgressBar(double initialProgress) {
		ProgressBar bar = new ProgressBar(0, 100);
		bar.setHeight("20%");
		bar.setWidth("100%");
		bar.getStyle().setDisplay(Display.INLINE_BLOCK);
		bar.getStyle().setBottom("0");
		bar.getStyle().setPosition(Position.ABSOLUTE);
		bar.getStyle().setMargin("0");
		bar.setValue(initialProgress);
		return bar;
	}

	private void addDynamicBackgroundContextMenu(Gantt gantt) {
		ContextMenu backgroundContextMenu = new ContextMenu();
		backgroundContextMenu.setTarget(gantt);
		gantt.getElement().addEventListener("vaadin-context-menu-before-open", event -> {
			backgroundContextMenu.removeAll();
			backgroundContextMenu.addItem("Add step at index " + clickedBackgroundIndex,
					e -> onHandleAddStepContextMenuAction(clickedBackgroundIndex, clickedBackgroundDate));
			var targetStep = gantt.getStepsList().get(clickedBackgroundIndex);
			backgroundContextMenu.addItem("Add sub-step for " + targetStep.getCaption(),
					e -> onHandleAddSubStepContextMenuAction(targetStep.getUid()));
			backgroundContextMenu.add(new Hr());
			backgroundContextMenu.addItem("Remove step " + targetStep.getCaption(),
					e -> onHandleRemoveStepContextMenuAction(targetStep.getUid()));
			backgroundContextMenu.add(new Hr());
			backgroundContextMenu.add(createProgressEditor(gantt.getStepElement(targetStep.getUid())));
		});
	}
	
	private void addDynamicSubStepContextMenu(StepElement stepElement) {
		stepElement.addContextMenu((contextMenu, uid) -> {
			contextMenu.removeAll();
			contextMenu.addItem("Add step at index " + clickedBackgroundIndex,
					e -> onHandleAddStepContextMenuAction(clickedBackgroundIndex, stepElement.getStartDateTime()));
			var targetStep = gantt.getStepsList().get(clickedBackgroundIndex);
			contextMenu.addItem("Add sub-step for " + targetStep.getCaption(),
					e -> onHandleAddSubStepContextMenuAction(targetStep.getUid()));
			contextMenu.add(new Hr());
			contextMenu.addItem("Remove step " + stepElement.getCaption(),
					e -> onHandleRemoveStepContextMenuAction(uid));
			contextMenu.add(new Hr());
			contextMenu.add(createProgressEditor(stepElement));
		});
	}
	
	private IntegerField createProgressEditor(StepElement stepElement) {
		var field = new IntegerField();
		field.setSuffixComponent(new Span("%"));
		field.setPlaceholder("Set progress");
		field.setStep(5);
		field.setStepButtonsVisible(true);
		field.setMin(0);
		field.setMax(100);
		// set initial value from first found progress bar component
		field.setValue(stepElement.getChildren()
				.filter(ProgressBar.class::isInstance).findFirst()
				.map(ProgressBar.class::cast).map(progressBar -> progressBar.getValue()).map(Double::intValue)
				.orElse(null));
		field.addValueChangeListener(ev -> {
			if (ev.getValue() > 0 && !stepElement.getChildren()
					.anyMatch(ProgressBar.class::isInstance)) {
				stepElement.add(createProgressBar(0));
			}
			// updates step's all progress bar components
			stepElement.getChildren()
					.filter(ProgressBar.class::isInstance).map(ProgressBar.class::cast)
					.forEach(progressBar -> {
						progressBar.setValue((ev.getValue()) % 101);
					});
		});
		return field;
	}

	private void onHandleRemoveStepContextMenuAction(String uid) {
		gantt.removeAnyStep(uid);
	}
	
	private void onHandleAddSubStepContextMenuAction(String uid) {
		var substep = createDefaultSubStep(uid);
		gantt.addSubStep(substep);
		addDynamicSubStepContextMenu(gantt.getStepElement(substep.getUid()));
		
	}
	
	private void onHandleAddStepContextMenuAction(int index, LocalDateTime startDate) {
		var step = createDefaultNewStep();
		if(startDate != null) {
			step.setStartDate(startDate);
			step.setEndDate(startDate.plusDays(7));
		}
		gantt.addStep(index, step);
	}
	
	private void onGanttBackgroundClick(GanttClickEvent event) {
		clickedBackgroundIndex = event.getIndex() != null ? event.getIndex() : 0;
		clickedBackgroundDate = event.getDate();
		if(event.getButton() == 2) {
			Notification.show("Clicked with mouse 2 at index: " + event.getIndex());
		} else {
			Notification.show("Clicked at index: " + event.getIndex() + " at date " + event.getDate().format(DateTimeFormatter.ofPattern("M/d/yyyy HH:mm")));
		}
	}
	
	private void onGanttStepClick(StepClickEvent event) {
		clickedBackgroundIndex = event.getIndex();
		Notification.show("Clicked step " + event.getAnyStep().getCaption());
	}
	
	private Div buildControlPanel() {
    	Div div = new Div();
    	div.setWidthFull();
    	
    	MenuBar menu = buildMenu();
    	HorizontalLayout tools = createTools();
    	div.add(menu, tools);
    	return div;
    }
    
    private HorizontalLayout createTools() {
    	HorizontalLayout tools = new HorizontalLayout();
    	Select<Resolution> resolutionField = new Select<Resolution>();
		resolutionField.setItems(Resolution.values());
    	resolutionField.setLabel("Resolution");
		resolutionField.setValue(gantt.getResolution());
		resolutionField.addValueChangeListener(event -> {
			gantt.setResolution(event.getValue());
			if(event.getValue() == Resolution.Hour) {
				gantt.setStartDateTime(startDateField.getValue().atTime(startTimeField.getValue()));
				gantt.setEndDateTime(endDateField.getValue().atTime(endTimeField.getValue()));
			} else {
				 gantt.setStartDate(startDateField.getValue());
				 gantt.setEndDate(endDateField.getValue());
			}
			setupToolsByResolution(event.getValue());
		});
    	
		startDateField = new DatePicker(gantt.getStartDate());
		startDateField.setLabel("Start Date");
		startDateField.addValueChangeListener(event -> gantt.setStartDate(event.getValue()));
    	
    	startTimeField = new TimePicker("Start Time", gantt.getStartDateTime().toLocalTime());
    	startTimeField.setWidth("8em");
    	startTimeField.addValueChangeListener(
				event -> gantt.setStartDateTime(startDateField.getValue().atTime(event.getValue())));
		
    	endDateField = new DatePicker(gantt.getEndDate());
    	endDateField.setLabel("End Date");
    	endDateField.addValueChangeListener(
				event -> gantt.setEndDate(event.getValue()));
		
		endTimeField = new TimePicker("End Time (inclusive)", gantt.getEndDateTime().toLocalTime());
		endTimeField.setWidth("8em");
		endTimeField.addValueChangeListener(
				event -> gantt.setEndDateTime(endDateField.getValue().atTime(event.getValue())));
		
		tools.add(resolutionField, startDateField, startTimeField, endDateField, endTimeField);
		tools.add(createTimeZoneField(gantt));
		tools.add(createLocaleField(gantt));
		
		setupToolsByResolution(gantt.getResolution());
		return tools;
    }

	private void setupToolsByResolution(Resolution value) {
		if(Resolution.Hour.equals(value)) {
			startTimeField.setVisible(true);
			endTimeField.setVisible(true);
		} else {
			startTimeField.setVisible(false);
			endTimeField.setVisible(false);
		}
	}
	
	private ComboBox<String> createTimeZoneField(Gantt gantt) {
		ComboBox<String> timeZoneField = new ComboBox<>("Timezone", getSupportedTimeZoneIds());
		timeZoneField.setWidth("350px");
		timeZoneField.setValue("Default");
		timeZoneField.setItemLabelGenerator(item -> {
			if ("Default".equals(item)) {
				return "Default (" + getDefaultTimeZone().getDisplayName(TextStyle.FULL, UI.getCurrent().getLocale())
						+ ")";
			}
			TimeZone tz = TimeZone.getTimeZone(item);
			return tz.getID() + " (raw offset " + (tz.getRawOffset() / 60000) + "m)";
		});
		timeZoneField.addValueChangeListener(e -> Optional.ofNullable(e.getValue()).ifPresent(zoneId -> {
			if ("Default".equals(zoneId)) {
				gantt.setTimeZone(TimeZone.getTimeZone(getDefaultTimeZone()));
			} else {
				gantt.setTimeZone(TimeZone.getTimeZone(ZoneId.of(zoneId)));
			}
		}));
		return timeZoneField;
	}

	private ComboBox<Locale> createLocaleField(Gantt gantt) {
		ComboBox<Locale> localeField = new ComboBox<>("Locale",
				Stream.of(Locale.getAvailableLocales()).collect(Collectors.toList()));
		localeField.setWidth("350px");
		localeField.setItemLabelGenerator((l) -> l.getDisplayName(UI.getCurrent().getLocale()));
		localeField.setValue(gantt.getLocale());
		localeField.addValueChangeListener(e -> Optional.ofNullable(e.getValue()).ifPresent(l -> gantt.setLocale(l)));
		return localeField;
	}

	private MenuBar buildMenu() {

		MenuBar menu = new MenuBar();
		MenuItem menuView = menu.addItem("View");
		MenuItem size = menuView.getSubMenu().addItem("Size");
		MenuItem size100x100 = size.getSubMenu().addItem(SizeOption.FULL_SIZE.getText());
		size100x100.setChecked(this.size == SizeOption.FULL_SIZE);
		size100x100.setCheckable(true);
		MenuItem size100xAuto = size.getSubMenu().addItem(SizeOption.FULL_WIDTH.getText());
		size100xAuto.setCheckable(true);
		size100xAuto.setChecked(this.size == SizeOption.FULL_WIDTH);
		MenuItem size50x100 = size.getSubMenu().addItem(SizeOption.HALF_WIDTH.getText());
		size50x100.setCheckable(true);
		size100x100.setChecked(this.size == SizeOption.HALF_WIDTH);
		MenuItem size100x50 = size.getSubMenu().addItem(SizeOption.HALF_HEIGHT.getText());
		size100x50.setCheckable(true);
		size100x100.setChecked(this.size == SizeOption.HALF_HEIGHT);
		
		size100x100.addClickListener(event -> {
			setSize(SizeOption.FULL_SIZE);
			event.getSource().setChecked(true);
			size100xAuto.setChecked(false);
			size100x50.setChecked(false);
			size50x100.setChecked(false);
		});
		size100xAuto.addClickListener(event -> {
			setSize(SizeOption.FULL_WIDTH);
			event.getSource().setChecked(true);
			size100x100.setChecked(false);
			size100x50.setChecked(false);
			size50x100.setChecked(false);
		});
		size50x100.addClickListener(event -> {
			setSize(SizeOption.HALF_WIDTH);
			event.getSource().setChecked(true);
			size100xAuto.setChecked(false);
			size100x100.setChecked(false);
			size100x50.setChecked(false);
		});
		size100x50.addClickListener(event -> {
			setSize(SizeOption.HALF_HEIGHT);
			event.getSource().setChecked(true);
			size100xAuto.setChecked(false);
			size100x100.setChecked(false);
			size50x100.setChecked(false);
		});
		
		MenuItem twelveHourClock = menuView.getSubMenu().addItem("Twelve hour clock");
		twelveHourClock.addClickListener(event -> {
			gantt.setTwelveHourClock(event.getSource().isChecked());
		});
		twelveHourClock.setCheckable(true);
		twelveHourClock.setChecked(gantt.isTwelveHourClock());

		MenuItem showYear = menuView.getSubMenu().addItem("Show year");
		showYear.addClickListener(event -> {
			gantt.setYearRowVisible(event.getSource().isChecked());
		});
		showYear.setCheckable(true);
		showYear.setChecked(gantt.isYearRowVisible());

		MenuItem showMonth = menuView.getSubMenu().addItem("Show month");
		showMonth.addClickListener(event -> {
			gantt.setMonthRowVisible(event.getSource().isChecked());
		});
		showMonth.setCheckable(true);
		showMonth.setChecked(gantt.isMonthRowVisible());
		
		MenuItem showCaptionGrid = menuView.getSubMenu().addItem("Show Caption Grid");
		showCaptionGrid.addClickListener(event -> {
			if(event.getSource().isChecked()) {
				buildCaptionGrid();
				scrollWrapper.addComponentAsFirst(grid);
			} else {
				gantt.removeCaptionGrid();
				scrollWrapper.remove(grid);
				grid = null;
			}
			setSize(this.size);
		});
		showCaptionGrid.setCheckable(true);
		showCaptionGrid.setChecked(grid.isVisible());

		MenuItem menuEdit = menu.addItem("Edit");
		
		MenuItem movableSteps = menuEdit.getSubMenu().addItem("Movable steps");
		movableSteps.addClickListener(event -> {
			gantt.setMovableSteps(event.getSource().isChecked());
		});
		movableSteps.setCheckable(true);
		movableSteps.setChecked(gantt.isMovableSteps());
		
		MenuItem resizableSteps = menuEdit.getSubMenu().addItem("Resizable steps");
		resizableSteps.addClickListener(event -> {
			gantt.setResizableSteps(event.getSource().isChecked());
		});
		resizableSteps.setCheckable(true);
		resizableSteps.setChecked(gantt.isResizableSteps());
		
		MenuItem movableStepsBetweenRows = menuEdit.getSubMenu().addItem("Movable steps between rows");
		movableStepsBetweenRows.addClickListener(event -> {
			gantt.setMovableStepsBetweenRows(event.getSource().isChecked());
		});
		movableStepsBetweenRows.setCheckable(true);
		movableStepsBetweenRows.setChecked(gantt.isMovableStepsBetweenRows());
		
		MenuItem menuAdd = menu.addItem("Add new Step");
		menuAdd.addClickListener(event -> insertNewStep());
		
		return menu;
	}
	
	private void setSize(SizeOption newSize) {
		this.size = newSize;
		switch (size) {
		case FULL_SIZE:
			setSizeFull();
			gantt.setWidth("70%");
			grid.setWidth("30%");
			gantt.setHeight("100%");
			grid.setHeight("100%");
			setFlexGrow(1, scrollWrapper);
			break;
		case FULL_WIDTH:
			setWidthFull();
			setHeight(null);
			gantt.setWidth("70%");
			grid.setWidth("30%");
			gantt.setHeight(null);
			grid.setHeight(null);
			grid.setAllRowsVisible(true);
			setFlexGrow(0, scrollWrapper);
			break;
		case HALF_WIDTH:
			setSizeFull();
			gantt.setWidth("40%");
			grid.setWidth("10%");
			gantt.setHeight("100%");
			grid.setHeight("100%");
			setFlexGrow(1, scrollWrapper);
			break;
		case HALF_HEIGHT:
			setSizeFull();
			gantt.setWidth("70%");
			grid.setWidth("30%");
			gantt.setHeight("50%");
			grid.setHeight("50%");
			setFlexGrow(1, scrollWrapper);
			break;
		}
	}
	
	private ZoneId getDefaultTimeZone() {
		ZoneId zone = ZoneId.systemDefault();
		return zone;
	}

	private List<String> getSupportedTimeZoneIds() {
		List<String> items = new ArrayList<>();
		items.add("Default");
		items.addAll(Arrays.asList(TimeZone.getAvailableIDs()));
		return items;
	}
	
	private void insertNewStep() {
		var step = createDefaultNewStep();
		gantt.addStep(step);
	}

	private Step createDefaultNewStep() {
		Step step = new Step();
		step.setCaption("New Step " + ++stepCounter);
		step.setBackgroundColor(String.format("#%06x", new Random().nextInt(0xffffff + 1)));
		step.setStartDate(LocalDateTime.of(2020, 4, 7, 0, 0));
		step.setEndDate(LocalDateTime.of(2020, 4, 14, 0, 0));
		return step;
	}
	
	private SubStep createDefaultSubStep(String ownerUid) {
		var owner = gantt.getStep(ownerUid);
		SubStep substep = new SubStep(owner);
		substep.setCaption("New Sub Step");
		substep.setBackgroundColor(String.format("#%06x", new Random().nextInt(0xffffff + 1)));
		if(gantt.getSubStepElements(ownerUid).count() == 0) {
			substep.setStartDate(owner.getStartDate());
			substep.setEndDate(owner.getEndDate());
		} else {
			substep.setStartDate(owner.getEndDate());
			substep.setEndDate(owner.getEndDate().plusDays(7));
			owner.setEndDate(substep.getEndDate());
			gantt.refresh(ownerUid);
		}
		return substep;
	}
	
	enum SizeOption {
		FULL_SIZE("100% x 100%"),
		FULL_WIDTH("100% x auto"),
		HALF_WIDTH("50% x 100%"),
		HALF_HEIGHT("100% x 50%");
		
		private String text;
		private SizeOption(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
	}
}
