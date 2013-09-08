package net.ladieu.biometrics.keystroke.model;

import java.io.Serializable;

/**
 * Represents a keystroke by incorporating dynamic attributes based on key press
 * time, release time, and prior keystroke information.
 * 
 * @author jrladieu
 */
public class Keystroke implements Serializable {

	private static final long serialVersionUID = -3513098364791818999L;

	private boolean endTimeCaptured;

	private char value;
	private long endTime;
	private long startTime;
	private Keystroke prior;
	private Keystroke next;

	public Keystroke(char value, long startTime) {
		super();
		this.value = value;

		if (startTime < 0) {
			throw new IllegalArgumentException("startTime provided was ["
					+ startTime + "]; startTime can not be negative");
		}

		this.startTime = startTime;
	}

	public void setPrior(Keystroke prior) {
		if (null == prior) {
			throw new IllegalArgumentException("attempt to set null prior");
		}

		if (null != this.prior) {
			throw new KeystrokeException("attempt to set prior ["
					+ prior.getValue() + "] when prior ["
					+ this.prior.getValue() + "] already set");
		}

		if (prior.getStartTime() > this.startTime) {
			throw new KeystrokeException(
					"attempt to set prior with startTime ["
							+ prior.getStartTime()
							+ "] which is later than current keystroke startTime ["
							+ this.startTime + "]");
		}

		this.prior = prior;
	}

	public Keystroke getPrior() {
		return this.prior;
	}

	public void setNext(Keystroke next) {
		if (null == next) {
			throw new IllegalArgumentException("attempt to set null next");
		}

		if (null != this.next) {
			throw new KeystrokeException("attempt to set next["
					+ next.getValue() + "] when next [" + this.next.getValue()
					+ "] already set");
		}

		if (next.getStartTime() < this.startTime) {
			throw new KeystrokeException("attempt to set next with startTime ["
					+ next.getStartTime()
					+ "] which is earlier than current keystroke startTime ["
					+ this.startTime + "]");
		}

		this.next = next;
	}

	public Keystroke getNext() {
		return this.next;
	}

	public boolean isReleased() {
		return endTimeCaptured;
	}

	public void release(long endTime) {
		if (endTimeCaptured) {
			throw new KeystrokeException("release already detected at ["
					+ this.endTime + "], attempted recapture at [" + endTime
					+ "]");
		}

		if (endTime < startTime) {
			throw new KeystrokeException("endTime [" + endTime
					+ "] less than startTime [" + startTime + "]");
		}

		this.endTime = endTime;
		endTimeCaptured = true;
	}

	public long getDwellTime() {
		if (endTimeCaptured) {
			return endTime - startTime;
		} else {
			return 0;
		}
	}

	public long getFlightTime() {

		if (null == prior || 0 == prior.getEndTime()) {
			return 0;
		} else {
			return this.startTime - prior.getEndTime();
		}
	}

	public char getValue() {
		return value;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append('[');
		result.append(getValue());
		result.append("- flight: ");
		result.append(getFlightTime());
		result.append(", dwell: ");
		result.append(getDwellTime());
		result.append(']');

		return result.toString();
	}
}
