package org.vaadin.tltv.gantt.model;

import org.vaadin.tltv.gantt.Gantt;
import org.vaadin.tltv.gantt.element.StepElement;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class describing a step inside {@link Gantt} component.
 */
public abstract class GanttStep {

    private Long identifier;
    private String uid = "";
    private String captionMode = "TEXT";
    private String styleName = "";
    private String caption = "";
    private String description = "";
    private String backgroundColor = "#A8D9FF";
    private double progress;
    private boolean showProgress;
    private boolean resizable = true;
    private boolean movable = true;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean substep;

    /** Application specific optional identifier. */
    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    /**
     * Unique indetifier for the step. UID is auto-generated for new steps when not
     * set explicitly.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Set unique identifier for this step. It's not recommended to set this
     * explicitly as it will be auto-generated when step is added first time into
     * Gantt and UID is not already set.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Deprecated
    public String getCaptionMode() {
        return captionMode;
    }

    @Deprecated
    public void setCaptionMode(String captionMode) {
        this.captionMode = captionMode;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    /**
     * Get caption text.
     * 
     * @return Caption text
     */
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Gets a description text.
     */
    @Deprecated
    public String getDescription() {
        return description;
    }

    /**
     * Sets a description text.
     * 
     * @param description description text
     * @deprecated Not shown anywhere. Use {@link StepElement#addTooltip(String)}
     *             instead. Placeholder for backwards compatibility with Vaadin 8
     *             Gantt API.
     */
    @Deprecated
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets background color like '#000000', 'red' or null if not set.
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color like '#000000' or 'red'. null clears it.
     */
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public double getProgress() {
        return progress;
    }

    /**
     * Sets progress.
     * 
     * @param progress Progress number
     * @deprecated Not shown anywhere. Use {@link StepElement#getElement()} to add a
     *             custom progress bar element and/or CSS to draw a progress bar.
     *             Placeholder for backwards compatibility with Vaadin 8 Gantt API.
     */
    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    /**
     * Sets showProgress boolean flag.
     * 
     * @param showProgress boolean
     * @deprecated Not shown anywhere. Use {@link StepElement#getElement()} to add a
     *             custom progress bar element and/or CSS to draw a progress bar.
     *             Placeholder for backwards compatibility with Vaadin 8 Gantt API.
     */
    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    /**
     * Get inclusive start date and time time in milliseconds.
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * Set inclusive start date and time in milliseconds.
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * Get inclusive end date and time time in milliseconds.
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * Set inclusive end date and time in milliseconds.
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isSubstep() {
        return substep;
    }

    public void setSubstep(boolean substep) {
        this.substep = substep;
    }

    @Override
    public int hashCode() {
        return uid != null ? Objects.hash(uid) : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(this instanceof GanttStep) || !(obj instanceof GanttStep)) {
            return false;
        }
        GanttStep other = (GanttStep) obj;
        if (uid == null) {
            if (other.uid != null) {
                return false;
            }
        } else if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

}
