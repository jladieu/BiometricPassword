package net.ladieu.biometrics.keystroke.model;

import static org.junit.Assert.*;

import net.ladieu.biometrics.keystroke.model.Keystroke;
import net.ladieu.biometrics.keystroke.model.KeystrokeSequence;
import net.ladieu.biometrics.keystroke.model.ExactPhraseMatcher;

import org.junit.Before;
import org.junit.Test;

public class ExactPhraseMatcherTest {

	private String expectedPhrase;
	private KeystrokeSequence sequence;
	private long tickingTime;
	private ExactPhraseMatcher templateUnderTest;

	@Before
	public void setUp() {
		expectedPhrase = "foobar";
		tickingTime = 1;

		templateUnderTest = new ExactPhraseMatcher(expectedPhrase);
		sequence = new KeystrokeSequence();

	}

	private void addKeystroke(char c) {
		Keystroke keystroke = new Keystroke(c, tickingTime++);
		keystroke.release(tickingTime++);
		sequence.addKeystroke(keystroke);
	}

	@Test
	public void constructorRequiresAtLeastOneCharacter() {
		try {
			new ExactPhraseMatcher(null);
			fail("null is not a valid input for KeystrokeSequence phrase");
		} catch (IllegalArgumentException e) {
			// pass
		}

		try {
			new ExactPhraseMatcher("");
			fail("empty string is not a valid input for KeystrokeSequence phrase");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void matchesOnlyReturnsTrueOnceTheSequenceExactlyMatches() {
		addKeystroke('f');
		assertNoMatch(templateUnderTest.getDistance(sequence));

		addKeystroke('o');
		assertNoMatch(templateUnderTest.getDistance(sequence));

		addKeystroke('o');
		assertNoMatch(templateUnderTest.getDistance(sequence));

		addKeystroke('b');
		assertNoMatch(templateUnderTest.getDistance(sequence));

		addKeystroke('a');
		assertNoMatch(templateUnderTest.getDistance(sequence));

		addKeystroke('r');
		assertMatch(templateUnderTest.getDistance(sequence));

		// adding one more character should throw it off again
		addKeystroke('r');
		assertNoMatch(templateUnderTest.getDistance(sequence));
	}

	private void assertMatch(float matchValue) {
		assertEquals(KeystrokeMatcher.EXACT_MATCH, matchValue);
	}

	private void assertNoMatch(float matchValue) {
		assertEquals(KeystrokeMatcher.NO_MATCH, matchValue);
	}

}
