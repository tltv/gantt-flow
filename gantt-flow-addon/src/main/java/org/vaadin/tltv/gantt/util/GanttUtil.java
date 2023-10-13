package org.vaadin.tltv.gantt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.vaadin.tltv.gantt.model.Resolution;

public class GanttUtil {

	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH':'mm':'ss";
	private static final String DATE_HOUR_PATTERN = "yyyy-MM-dd'T'HH";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
	private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
	private final static DateTimeFormatter dateHourFormatter = DateTimeFormatter.ofPattern(DATE_HOUR_PATTERN);

	/**
	 * Format given {@link TemporalAccessor} to datetime format yyyy-MM-ddTHH:mm:ss.
	 * 
	 * @param temporal Target datetime
	 * @return Formatted datetime
	 */
	public static String formatDateTime(TemporalAccessor temporal) {
		return dateTimeFormatter.format(temporal);
	}

	public static TemporalAccessor parseDateTime(CharSequence text) {
		return dateTimeFormatter.parse(text);
	}
	
	/**
	 * Format given {@link TemporalAccessor} to date format yyyy-MM-dd.
	 * 
	 * @param temporal Target date
	 * @return Formatted date
	 */
	public static String formatDate(TemporalAccessor temporal) {
		return dateFormatter.format(temporal);
	}

	public static TemporalAccessor parseDate(CharSequence text) {
		return dateFormatter.parse(text);
	}
	
	public static String formatDateHour(TemporalAccessor temporal) {
		return dateHourFormatter.format(temporal);
	}

	public static TemporalAccessor parseDateHour(CharSequence text) {
		return dateHourFormatter.parse(text);
	}
	
	public static TemporalAccessor parse(CharSequence text) {
		if(text.length() > 13) {
			return parseDateTime(text);
		} else if(text.length() > 10) {
			return parseDateHour(text);
		}
		return parseDate(text);
	}
	
	public static LocalDateTime parseLocalDateTime(CharSequence text) {
		return LocalDateTime.from(parseDateTime(text.subSequence(0, 19)));
	}

	/**
	 * Reset given datetime to minimum for the given resolution. Truncates to {@link
	 * ChronoUnit#DAYS} for Day and Week resoutions. Truncates to {@link
	 * ChronoUnit#HOURS} for
	 * Hour resoution.
	 * 
	 * @param dateTime target datetime
	 * @param resolution target resolution
	 * @return Truncated {@link LocalDateTime} or null with a null date.
	 */
	public static LocalDateTime resetTimeToMin(LocalDateTime dateTime, Resolution resolution) {
		if (Objects.isNull(dateTime)) {
			return null;
		}
		if (Resolution.Hour.equals(resolution)) {
			return dateTime.truncatedTo(ChronoUnit.HOURS);
		}
		return dateTime.truncatedTo(ChronoUnit.DAYS);
	}

	/**
	 * Reset given datetime to timeline maximum for the given resolution. Truncates
	 * or adds to last second of the day for Day and Week resoutions. Truncates or
	 * adds to last second of hour for
	 * Hour resoution. Maximum can be <code>exclusive</code> which means that given
	 * date or hour is either excluded with a <code>true</code> or included with
	 * <code>false</code>.
	 * 
	 * @param dateTime   target datetime
	 * @param resolution target resolution
	 * @param exclusive  exclusive maximum
	 * @return Adjusted {@link LocalDateTime} or null with a null date.
	 */
	public static LocalDateTime resetTimeToMax(LocalDateTime dateTime, Resolution resolution, boolean exclusive) {
		if (Objects.isNull(dateTime)) {
			return null;
		}
		if (Resolution.Hour.equals(resolution)) {
			if (exclusive) {
				dateTime = dateTime.minusHours(1);
			}
			return dateTime.plusHours(1).truncatedTo(ChronoUnit.HOURS).minusSeconds(1);
		}
		if (exclusive) {
			dateTime = dateTime.minusDays(1);
		}
		return dateTime.plusDays(1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1);
	}
}
