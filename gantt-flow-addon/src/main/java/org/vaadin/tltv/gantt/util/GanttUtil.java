package org.vaadin.tltv.gantt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.vaadin.tltv.gantt.model.Resolution;

public class GanttUtil {

	private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

	public static String formatDateTime(TemporalAccessor temporal) {
		return dateTimeFormatter.format(temporal);
	}

	public static TemporalAccessor parseDateTime(CharSequence text) {
		return dateTimeFormatter.parse(text);
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
