package org.vaadin.tltv.gantt.element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.shared.Registration;

/**
 * {@link StepElement} represents <code>gantt-step-element</code> element in the DOM.
 */
@Tag("gantt-step-element")
public class StepElement extends Component {

	private final String uid;
	
	private GanttStep model;
	
	private final List<BiConsumer<ContextMenu, String>> contextMenuBuilders = new ArrayList<>();
	private final List<Registration> contextMenuDomListenerRegistrations = new ArrayList<>();
	
	public StepElement(GanttStep model) {
		this.model = model;
		this.uid = model.getUid();
		
		getElement().setProperty("uid", this.uid);
		refresh();
	}
	
	public void refresh() {
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

	/**
	 * Remove element from the parent and clear all context menu listeners ({@link #getContextMenuBuilders()}).
	 */
	public void removeFromParent() {
		contextMenuDomListenerRegistrations.forEach(Registration::remove);
		getElement().removeFromParent();
	}
	
	/**
	 * Adds dynamic context menu. Builder is responsible rebuilding the context menu
	 * items for the given UID.
	 * 
	 * @param builder Context menu builder with {@link ContextMenu} instance to
	 *                build and target step's UID.
	 */
	public void addContextMenu(BiConsumer<ContextMenu, String> builder) {
		this.contextMenuBuilders.add(builder);
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setTarget(this);
		var reg = getElement().addEventListener("vaadin-context-menu-before-open", event -> {
			builder.accept(contextMenu, event.getEventData().getString("element.uid"));
		}).addEventData("element.uid");
		contextMenuDomListenerRegistrations.add(new Registration() {
			private static final long serialVersionUID = 7711223599693115951L;
			@Override
			public void remove() {
				reg.remove();
				contextMenuBuilders.remove(builder);
			}
		});
	}
	
	public List<BiConsumer<ContextMenu, String>> getContextMenuBuilders() {
		return contextMenuBuilders.stream().collect(Collectors.toList());
	}
}
