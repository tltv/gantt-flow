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

	public static String formatDateTime(TemporalAccessor temporal) {
		return dateTimeFormatter.format(temporal);
	}

	public static TemporalAccessor parseDateTime(CharSequence text) {
		return dateTimeFormatter.parse(text);
	}
	
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

	public static LocalDateTime resetTimeToMin(LocalDateTime dateTime, Resolution resolution) {
		if (Objects.isNull(dateTime)) {
			return null;
		}
		if (Resolution.Hour.equals(resolution)) {
			return dateTime.truncatedTo(ChronoUnit.HOURS);
		}
		return dateTime.truncatedTo(ChronoUnit.DAYS);
	}

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
