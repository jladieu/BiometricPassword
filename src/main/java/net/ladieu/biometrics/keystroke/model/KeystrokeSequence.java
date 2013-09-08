package net.ladieu.biometrics.keystroke.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeystrokeSequence implements Iterable<Keystroke>, Serializable {

	private static final long serialVersionUID = -7106016073755020475L;
	private List<Keystroke> keystrokes;
	private String capturedValue;

	public KeystrokeSequence() {
		super();
		keystrokes = new ArrayList<Keystroke>();
		capturedValue = "";
	}

	public String getCapturedValue() {
		return capturedValue;
	}

	public void addKeystroke(Keystroke keystroke) {
		if (null == keystroke) {
			throw new IllegalArgumentException("addKeystroke(null) not allowed");
		}

		capturedValue = capturedValue.concat(String.valueOf(keystroke
				.getValue()));

		linkToPriorKeystroke(keystroke);

		keystrokes.add(keystroke);
	}

	private void linkToPriorKeystroke(Keystroke keystroke) {
		Keystroke prior = getMostRecentKeystroke();

		if (null != prior) {
			keystroke.setPrior(prior);
			prior.setNext(keystroke);
		}
	}

	public Iterator<Keystroke> iterator() {
		List<Keystroke> clonedList = new ArrayList<Keystroke>(keystrokes);
		return clonedList.iterator();
	}

	public String toString() {
		StringBuilder message = new StringBuilder();

		message.append("\nKeySequence [" + getCapturedValue() + "]");
		for (Keystroke currentKeystroke : this) {
			message.append("\n\t");
			message.append(currentKeystroke);
		}

		return message.toString();
	}

	public KeystrokeSequence createCopy() {
		KeystrokeSequence copy = new KeystrokeSequence();
		copy.keystrokes = new ArrayList<Keystroke>(this.keystrokes);
		copy.capturedValue = new String(this.capturedValue);
		return copy;
	}

	public Keystroke getMostRecentKeystroke() {
		int length = keystrokes.size();
		if (length > 0) {
			return keystrokes.get(length - 1);
		}

		return null;
	}

	public Keystroke getKeystroke(int index) {
		return keystrokes.get(index);
	}

	public Keystroke getFirstKeystroke() {
		if (!keystrokes.isEmpty()) {
			return keystrokes.get(0);
		}

		return null;
	}

}
