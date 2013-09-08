package net.ladieu.biometrics.keystroke.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeystrokeRecorderImpl implements KeystrokeRecorder {

	private Map<Character, Keystroke> activeKeys;

	private KeystrokeFactory factory;
	private KeystrokeSequence sequence;

	public KeystrokeRecorderImpl(KeystrokeFactory factory) {
		super();
		if (null == factory) {
			throw new IllegalArgumentException("factory must be non-null");
		}
		this.factory = factory;
		activeKeys = new HashMap<Character, Keystroke>();
		reset();
	}

	public KeystrokeSequence getResult() {
		return sequence.createCopy();
	}

	public boolean keyDown(char key, long time) {

		if (!activeKeys.containsKey(key)) {
			if (!factory.acceptsCharacter(key)) {
				throw new KeystrokeException("key [" + key + "] not allowed");
			}

			Keystroke newKeystroke = factory.createKeystroke(key, time);
			activeKeys.put(newKeystroke.getValue(), newKeystroke);
			sequence.addKeystroke(newKeystroke);
			return true;
		}

		return false;
	}

	public boolean keyUp(char key, long time) {

		Keystroke keystroke = activeKeys.remove(key);

		if (null != keystroke) {
			keystroke.release(time);
				
			return true;
		}

		return false;
	}
	

	public Set<Character> getActiveKeys() {
		return new HashSet<Character>(activeKeys.keySet());
	}

	public void reset() {
		sequence = new KeystrokeSequence();
	}

}
