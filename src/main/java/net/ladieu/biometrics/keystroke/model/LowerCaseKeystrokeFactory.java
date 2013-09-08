package net.ladieu.biometrics.keystroke.model;

import java.util.HashSet;
import java.util.Set;

public class LowerCaseKeystrokeFactory implements KeystrokeFactory {

	private final Set<Character> validCharacters;

	public LowerCaseKeystrokeFactory() {
		this(KeystrokeCharacterSets.DEFAULT_CHARSET);
	}

	public LowerCaseKeystrokeFactory(Set<Character> validCharacters) {
		super();
		if (null == validCharacters) {
			throw new IllegalArgumentException(
					"validCharacters must not be null");
		}
		if (validCharacters.isEmpty()) {
			throw new IllegalArgumentException(
					"validCharacters must not be empty");
		}

		this.validCharacters = new HashSet<Character>(validCharacters.size());

		for (Character c : validCharacters) {
			this.validCharacters.add(Character.toLowerCase(c));
		}
	}

	/**
	 * Creates a Keystroke for the given character, at the given startTime
	 */
	public Keystroke createKeystroke(char c, long startTime) {

		char lowerCaseInput = Character.toLowerCase(c);

		if (!validCharacters.contains(lowerCaseInput)) {
			throw new IllegalArgumentException("'" + c
					+ "' is not a valid character");
		}

		return new Keystroke(lowerCaseInput, startTime);
	}

	public boolean acceptsCharacter(char c) {
		return validCharacters.contains(Character.toLowerCase(c));
	}

}
