package net.ladieu.biometrics.keystroke.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeystrokeCharacterSets {

	private KeystrokeCharacterSets() {
		super();
	}

	public static final Set<Character> DEFAULT_CHARSET = new HashSet<Character>(
			Arrays.asList(new Character[] { '0', '1', '2', '3', '4', '5', '6',
					'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
					'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
					'v', 'w', 'x', 'y', 'z' }));
}
