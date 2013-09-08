package net.ladieu.biometrics.keystroke.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.ladieu.biometrics.keystroke.model.Keystroke;
import net.ladieu.biometrics.keystroke.model.KeystrokeFactory;
import net.ladieu.biometrics.keystroke.model.LowerCaseKeystrokeFactory;

import org.junit.Before;
import org.junit.Test;

public class LowercaseKeystrokeFactoryTest {

	private final Set<Character> THREE_LOWERCASE_PLUS_SPECIAL_CHARACTERS = new HashSet<Character>(
			Arrays.asList(new Character[] { 'a', 'b', 'c', ' ', '!' }));

	private KeystrokeFactory factoryUnderTest;
	private long validTime;

	@Before
	public void setUp() {
		factoryUnderTest = new LowerCaseKeystrokeFactory(
				THREE_LOWERCASE_PLUS_SPECIAL_CHARACTERS);
		validTime = System.currentTimeMillis();
	}

	@Test
	public void constructorFailsIfNoCharactersAccepted() {
		assertConstructorThrowsIllegalArgumentException(
				"null Set for valid characters provided", null);
		assertConstructorThrowsIllegalArgumentException(
				"empty Set for valid characters provided",
				new HashSet<Character>(0));
	}

	@Test
	public void getValidCharactersReturnsListProvidedInConstructorIfAllLowerCaseProvided() {

		assertGetValidCharactersMatches(
				"single lowercase character should be unchanged",
				new Character[] { 'c' }, new Character[] { 'c' });

		assertGetValidCharactersMatches(
				"two lowercase characters should be unchanged",
				new Character[] { 'c', 'd' }, new Character[] { 'c', 'd' });

		assertGetValidCharactersMatches(
				"three lowercase characters should be unchanged",
				new Character[] { 'a', 'b', 'c' }, new Character[] { 'a', 'b',
						'c' });

		assertGetValidCharactersMatches(
				"lowercase and special characters should be unchanged",
				new Character[] { 'a', 'b', 'c', ' ', '!' },
				new Character[] { 'a', 'b', 'c', ' ', '!' });
	}

	@Test
	public void constructorLowerCasesAllCharactersProvidedInInputList() {

		assertGetValidCharactersMatches(
				"single uppercase should be converted to lowercase",
				new Character[] { 'c' }, new Character[] { 'C' });

		assertGetValidCharactersMatches(
				"redundant upper and lowercase should be consolidated",
				new Character[] { 'c' }, new Character[] { 'C', 'c' });

		assertGetValidCharactersMatches(
				"mixed case and special characters should be selectively lowercased",
				new Character[] { 'a', 'b', 'c', ' ', 'x', '2' },
				new Character[] { 'a', 'B', 'C', ' ', 'X', '2' });

	}

	@Test
	public void createKeystrokeSucceedsWhenCharacterInListAndTimeIsValid() {

		for (char c : THREE_LOWERCASE_PLUS_SPECIAL_CHARACTERS) {
			assertCreateKeystrokeReturnsWithValueOfExpectedChar(
					"valid lowercase characters should create keystrokes correctly",
					c, c, validTime);
		}
	}

	@Test
	public void createKeystrokeSucceedsWhenCharacterIsUppercaseButInListAndTimeIsValid() {
		for (char c : THREE_LOWERCASE_PLUS_SPECIAL_CHARACTERS) {
			assertCreateKeystrokeReturnsWithValueOfExpectedChar(
					"valid lowercase characters should create keystrokes correctly",
					c, Character.toUpperCase(c), validTime);
		}
	}

	@Test
	public void createKeystrokeFailsWhenCharacterNotValid() {

		assertCreateKeystrokeThrowsIllegalArgumentException("'d' not in list",
				'd', validTime);
		assertCreateKeystrokeThrowsIllegalArgumentException("'~' not in list",
				'~', validTime);
		assertCreateKeystrokeThrowsIllegalArgumentException(
				"'\u0000' not in list", '\u0000', validTime);
	}

	@Test
	public void createKeystrokeFailsWhenTimeInvalid() {

		assertCreateKeystrokeThrowsIllegalArgumentException(
				"startTime is negative", 'a', -1);
		assertCreateKeystrokeThrowsIllegalArgumentException(
				"startTime is negative", 'a', -10000);
		assertCreateKeystrokeThrowsIllegalArgumentException(
				"startTime is negative", 'a', Long.MIN_VALUE);

	}

	private void assertGetValidCharactersMatches(String reason,
			Character[] expected, Character[] input) {

		LowerCaseKeystrokeFactory factory = new LowerCaseKeystrokeFactory(new HashSet<Character>(Arrays.asList(input)));
		for (Character currentChar : expected) {
			assertTrue(reason, factory.acceptsCharacter(currentChar));
		}
	}

	private void assertConstructorThrowsIllegalArgumentException(String reason,
			Set<Character> validCharacters) {

		try {
			new LowerCaseKeystrokeFactory(validCharacters);
			fail("Expected IllegalArgumentException because " + reason);
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	private void assertCreateKeystrokeReturnsWithValueOfExpectedChar(
			String reason, char expectedChar, char inputChar, long startTime) {
		Keystroke result = factoryUnderTest.createKeystroke(inputChar,
				startTime);
		assertNotNull(result);

		assertEquals(reason, expectedChar, result.getValue());
	}

	private void assertCreateKeystrokeThrowsIllegalArgumentException(
			String reason, char c, long startTime) {
		try {
			factoryUnderTest.createKeystroke(c, startTime);
			fail("Expected IllegalArgumentException because " + reason);
		} catch (IllegalArgumentException e) {
			// pass
		}
	}
}
