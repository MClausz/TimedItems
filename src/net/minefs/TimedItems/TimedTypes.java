package net.minefs.TimedItems;

public enum TimedTypes {
	SECOND(1000l), MINUTE(60000l), HOUR(3600000l), DAY(86400000l), WEEK(604800000l), MONTH(2592000000l), YEAR(
			31536000000l);
	private final long duration;

	TimedTypes(long duration) {
		this.duration = duration;
	}

	public long getDurarion() {
		return this.duration;
	}

	public static TimedTypes getType(String type) {
		switch (type) {
		case "giây":
			return TimedTypes.SECOND;
		case "phút":
			return TimedTypes.MINUTE;
		case "giờ":
			return TimedTypes.HOUR;
		case "ngày":
			return TimedTypes.DAY;
		case "tuần":
			return TimedTypes.WEEK;
		case "tháng":
			return TimedTypes.MONTH;
		case "năm":
			return TimedTypes.YEAR;
		default:
			return null;
		}
	}
}
