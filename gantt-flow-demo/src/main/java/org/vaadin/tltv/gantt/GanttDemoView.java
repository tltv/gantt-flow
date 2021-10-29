package org.vaadin.tltv.gantt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

@Route("")
public class GanttDemoView extends VerticalLayout {

	private Gantt gantt;
	private Div scrollWrapper;
	
	private TimePicker startTimeField;
	private TimePicker endTimeField;

	public GanttDemoView() {
		setWidthFull();
		setPadding(false);
		
		gantt = createGantt();
		
		Div controlPanel = buildControlPanel();

		scrollWrapper = new Div();
    	scrollWrapper.setId("scroll-wrapper");
    	scrollWrapper.setMinHeight("0");
    	scrollWrapper.setWidthFull();
    	scrollWrapper.add(gantt);
    	
        add(controlPanel, scrollWrapper);
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

		gantt.addStep(step1);
		gantt.addStep(step2);
		
		gantt.addGanttClickListener(event -> Notification.show("Clicked at index: " + event.getIndex()));
		gantt.addStepClickListener(event -> Notification.show("Clicked step " + event.getStep().getCaption()));
		return gantt;
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
    	Select<Resolution> resolutionField = new Select<Resolution>(Resolution.values());
    	resolutionField.setLabel("Resolution");
		resolutionField.setValue(gantt.getResolution());
		resolutionField.addValueChangeListener(event -> {
			gantt.setResolution(event.getValue());
			setupToolsByResolution(event.getValue());
		});
    	
    	DatePicker startDate = new DatePicker(gantt.getStartDateTime().toLocalDate());
    	startDate.setLabel("Start Date");
    	startDate.addValueChangeListener(event -> gantt.setStartDate(event.getValue()));
    	
    	startTimeField = new TimePicker("Start Time", gantt.getStartDateTime().toLocalTime());
    	startTimeField.setWidth("8em");
    	startTimeField.addValueChangeListener(
				event -> gantt.setStartDateTime(startDate.getValue().atTime(event.getValue())));
		
    	DatePicker endDate = new DatePicker(gantt.getEndDateTime().toLocalDate());
    	endDate.setLabel("End Date");
		endDate.addValueChangeListener(
				event -> gantt.setEndDate(event.getValue()));
		
		endTimeField = new TimePicker("End Time (inclusive)", gantt.getEndDateTime().toLocalTime());
		endTimeField.setWidth("8em");
		endTimeField.addValueChangeListener(
				event -> gantt.setEndDateTime(endDate.getValue().atTime(event.getValue())));
		
		tools.add(resolutionField, startDate, startTimeField, endDate, endTimeField);
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
		MenuItem size100x100 = size.getSubMenu().addItem("100% x 100%");
		size100x100.setCheckable(true);
		MenuItem size100xAuto = size.getSubMenu().addItem("100% x auto");
		size100xAuto.setCheckable(true);
		size100xAuto.setChecked(true);
		MenuItem size50x100 = size.getSubMenu().addItem("50% x 100%");
		size50x100.setCheckable(true);
		MenuItem size100x50 = size.getSubMenu().addItem("100% x 50%");
		size100x50.setCheckable(true);
		
		size100x100.addClickListener(event -> {
			event.getSource().setChecked(true);
			setSizeFull();
			gantt.setSizeFull();
			setFlexGrow(1, scrollWrapper);
			size100xAuto.setChecked(false);
			size100x50.setChecked(false);
			size50x100.setChecked(false);
		});
		size100xAuto.addClickListener(event -> {
			event.getSource().setChecked(true);
			setWidthFull();
			setHeight(null);
			gantt.setWidthFull();
			gantt.setHeight(null);
			setFlexGrow(0, scrollWrapper);
			size100x100.setChecked(false);
			size100x50.setChecked(false);
			size50x100.setChecked(false);
		});
		size50x100.addClickListener(event -> {
			event.getSource().setChecked(true);
			setSizeFull();
			gantt.setWidth("50%");
			gantt.setHeight("100%");
			setFlexGrow(1, scrollWrapper);
			size100xAuto.setChecked(false);
			size100x100.setChecked(false);
			size100x50.setChecked(false);
		});
		size100x50.addClickListener(event -> {
			event.getSource().setChecked(true);
			setSizeFull();
			gantt.setWidth("100%");
			gantt.setHeight("50%");
			setFlexGrow(1, scrollWrapper);
			size100xAuto.setChecked(false);
			size100x100.setChecked(false);
			size50x100.setChecked(false);
		});
		
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
		Step step = new Step();
		step.setCaption("New Step");
		step.setBackgroundColor("#fff");
		step.setStartDate(LocalDateTime.of(2020, 4, 7, 0, 0));
		step.setEndDate(LocalDateTime.of(2020, 4, 14, 0, 0));
		gantt.addStep(step);
		
	}
}
