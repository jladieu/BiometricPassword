package net.ladieu.biometrics.keystroke.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashSet;
import java.util.Set;

import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification.KeystrokeNotificationType;
import net.ladieu.biometrics.keystroke.model.KeystrokeException;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;
import net.ladieu.system.SystemTime;

public class KeystrokeDirector implements KeyListener {

	private static final char DEFAULT_COMPLETION_CHAR = '\n';

	private char completionChar;

	private boolean inputCompleted;
	private KeystrokeRecorder recorder;
	private Set<KeystrokeDirectorObserver> observers;

	public KeystrokeDirector() {
		this(DEFAULT_COMPLETION_CHAR);
	}

	public KeystrokeDirector(char completionChar) {
		super();
		this.completionChar = completionChar;
		observers = new LinkedHashSet<KeystrokeDirectorObserver>();
		inputCompleted = false;
	}

	public void setRecorder(KeystrokeRecorder recorder) {
		this.recorder = recorder;
	}

	public void addObserver(KeystrokeDirectorObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(KeystrokeDirectorObserver observer) {
		observers.remove(observer);
	}

	public void keyPressed(KeyEvent event) {

		long eventTime = SystemTime.asMillis();

		if (completionChar == event.getKeyChar()) {
			flagCompletion();
		} else {
			try {
				recorder.keyDown(Character.toLowerCase(event.getKeyChar()),
						eventTime);
			} catch (KeystrokeException e) {
				notifyObservers(KeystrokeNotificationType.CORRUPTION);
			}
		}

	}

	public void keyReleased(KeyEvent event) {
		long eventTime = SystemTime.asMillis();

		// notify observers if key up reports a state change
		if (recorder
				.keyUp(Character.toLowerCase(event.getKeyChar()), eventTime)) {
			notifyObservers(KeystrokeNotificationType.UPDATE);
		}

		if (isInputCompleted() && recorder.getActiveKeys().isEmpty()) {
			notifyObservers(KeystrokeNotificationType.COMPLETION);
		}
	}

	public void keyTyped(KeyEvent event) {
		// no-op
	}

	public void filterOutliers() {
		notifyObservers(KeystrokeNotificationType.FILTER);
	}

	public void refreshViews() {
		notifyObservers(KeystrokeNotificationType.REFRESH);
	}

	protected void notifyObservers(KeystrokeNotificationType type) {

		KeystrokeNotification event = new KeystrokeNotification(type, recorder);

		for (KeystrokeDirectorObserver observer : observers) {
			observer.notificationReceived(event);
		}
	}

	protected boolean isInputCompleted() {
		return inputCompleted;
	}

	protected void flagCompletion() {
		this.inputCompleted = true;
	}

	public void reset() {
		inputCompleted = false;
		recorder.reset();
	}
}
