package net.ladieu.biometrics.keystroke.view;

import net.ladieu.biometrics.keystroke.model.KeystrokeCharacterSets;

public class FieldValidator {

	public static boolean isValid(String inputText, int minLength) {

		if (null != inputText && inputText.length() >= minLength) {
			for (char c : inputText.toCharArray()) {
				if (!KeystrokeCharacterSets.DEFAULT_CHARSET.contains(Character
						.toLowerCase(c))) {
					return false;
				}
			}
			return true;
		}
		return false;

	}
}
