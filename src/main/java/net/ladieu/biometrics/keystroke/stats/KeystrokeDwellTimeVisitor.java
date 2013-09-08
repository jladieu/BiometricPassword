package net.ladieu.biometrics.keystroke.stats;

import net.ladieu.biometrics.keystroke.model.Keystroke;

public class KeystrokeDwellTimeVisitor implements KeystrokeVisitor<Number> {

	public Number visitKeystroke(Keystroke keystroke) {
		return keystroke.getDwellTime();
	}

}
