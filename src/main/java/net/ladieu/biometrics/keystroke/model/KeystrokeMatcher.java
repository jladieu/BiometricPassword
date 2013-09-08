package net.ladieu.biometrics.keystroke.model;

public interface KeystrokeMatcher {

	float EXACT_MATCH = 1.0f;
	float NO_MATCH = 0.0f;

	float getDistance(KeystrokeSequence sequence);

	String getTextToMatch();
}
