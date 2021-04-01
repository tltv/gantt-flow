package org.vaadin.tltv.gantt.model;

/**
 * Resolution of timeline.
 *
 * @author Tltv
 *
 */
public enum Resolution {

    /**
     * Day resolution makes the Gantt Chart to represent all dates in the
     * timeline. Suitable for representing few days or several weeks long steps.
     */
    Day,
    /**
     * Week resolution divides the timeline in week blocks making it clearer to
     * represent several months long steps.
     */
    Week,

    /**
     * Hour resolution divides the timeline in hour blocks.
     */
    Hour
}
