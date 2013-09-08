package net.ladieu.biometrics.keystroke.stats;

import net.ladieu.biometrics.keystroke.model.Keystroke;

public class KeystrokeFlightTimeVisitor implements KeystrokeVisitor<Number> {

	public Number visitKeystroke(Keystroke keystroke) {
		return keystroke.getFlightTime();
	}

}
