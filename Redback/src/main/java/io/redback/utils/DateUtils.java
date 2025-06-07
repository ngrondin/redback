package io.redback.utils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class DateUtils {

	public static Date convertDateToTimezone(Date in, String timezone) {
		ZoneId tz = timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();
		if(in != null) {
			ZoneOffset offset = tz.getRules().getOffset(in.toInstant());
			int offsetSeconds = offset.getTotalSeconds();
			long newTS = in.getTime() + (offsetSeconds * 1000);
			Date newDate = new Date(newTS);
			return newDate;			
		} else {
			return null;
		}
	}
}
