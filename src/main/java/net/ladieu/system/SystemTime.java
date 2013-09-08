package net.ladieu.system;

import java.util.Date;

public class SystemTime {

	private static final TimeSource DEFAULT_SOURCE = new TimeSource() {
		public long millis() {
			return System.currentTimeMillis();
		}
	};

	private static TimeSource source = null;

	public static long asMillis() {
		return getTimeSource().millis();
	}

	public static Date asDate() {
		return new Date(asMillis());
	}

	public static void reset() {
		setTimeSource(null);
	}

	public static void setTimeSource(TimeSource source) {
		SystemTime.source = source;
	}

	private static TimeSource getTimeSource() {
		return (null != source ? source : DEFAULT_SOURCE);
	}
}
