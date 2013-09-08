package net.ladieu.biometrics.keystroke.controller;

import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;

public class KeystrokeNotification {
	public enum KeystrokeNotificationType {
		UPDATE, CORRUPTION, COMPLETION, FILTER, REFRESH
	}

	private KeystrokeNotificationType type;
	private KeystrokeRecorder origin;

	public KeystrokeNotification(KeystrokeNotificationType type,
			KeystrokeRecorder origin) {
		super();
		if (null == type) {
			throw new IllegalArgumentException("type must not be null");
		}

		if (null == origin) {
			throw new IllegalArgumentException("origin must not be null");
		}
		this.type = type;
		this.origin = origin;
	}

	public KeystrokeNotificationType getType() {
		return type;
	}

	public KeystrokeRecorder getOrigin() {
		return origin;
	}

}
