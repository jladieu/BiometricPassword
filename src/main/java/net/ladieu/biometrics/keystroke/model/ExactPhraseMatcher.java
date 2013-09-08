package net.ladieu.biometrics.keystroke.model;

public class ExactPhraseMatcher implements KeystrokeMatcher {

	private String phrase;

	public ExactPhraseMatcher(String phrase) {
		super();
		if (null == phrase) {
			throw new IllegalArgumentException("password can't be null");
		}
		if (phrase.length() == 0) {
			throw new IllegalArgumentException("password can't be blank");
		}
		this.phrase = phrase;
	}

	public float getDistance(KeystrokeSequence sequence) {
		return phrase.equals(sequence.getCapturedValue()) ? KeystrokeMatcher.EXACT_MATCH
				: KeystrokeMatcher.NO_MATCH;
	}

	public String getTextToMatch() {
		return phrase;
	}

}
