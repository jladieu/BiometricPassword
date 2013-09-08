package net.ladieu.biometrics.keystroke.stats;

import net.ladieu.biometrics.keystroke.model.Keystroke;

public interface KeystrokeVisitor<T> {

	T visitKeystroke(Keystroke keystroke);
}
