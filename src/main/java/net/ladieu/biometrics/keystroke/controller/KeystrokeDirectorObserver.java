package net.ladieu.biometrics.keystroke.controller;


public interface KeystrokeDirectorObserver {
	
	void notificationReceived(KeystrokeNotification event);
}
