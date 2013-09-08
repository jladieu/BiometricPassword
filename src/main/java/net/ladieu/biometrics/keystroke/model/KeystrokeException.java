package net.ladieu.biometrics.keystroke.model;

/**
 * Simple exception to wrap problems capturing Keystrokes.
 * 
 * @author jrladieu
 */
public class KeystrokeException extends RuntimeException {

	private static final long serialVersionUID = 267055484575229629L;

	public KeystrokeException(String message) {
		super(message);
	}

}
