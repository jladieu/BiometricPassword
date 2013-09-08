package net.ladieu.biometrics.keystroke.model;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import net.ladieu.biometrics.keystroke.model.Keystroke;
import net.ladieu.biometrics.keystroke.model.KeystrokeFactory;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorderImpl;
import net.ladieu.biometrics.keystroke.model.LowerCaseKeystrokeFactory;
import net.ladieu.test.util.AbstractVerifyingMockObjectTest;

import org.junit.Test;

public class KeystrokeRecorderImplTest extends
		AbstractVerifyingMockObjectTest {

	private KeystrokeRecorder recorderUnderTest;
	private char testChar;
	private long testStart;
	private long tickingTime;

	public void onSetUp() {
		recorderUnderTest = new KeystrokeRecorderImpl(
				new LowerCaseKeystrokeFactory());
		testChar = 'a';
		testStart = 555;
		tickingTime = 1;
	}

	private KeystrokeFactory createMockFactoryExpecting(char expectedChar,
			long expectedTime, Keystroke returnValue) {

		KeystrokeFactory mockFactory = createMock(KeystrokeFactory.class);

		expect(mockFactory.createKeystroke(expectedChar, expectedTime))
				.andReturn(returnValue).once();

		expect(mockFactory.acceptsCharacter('a')).andReturn(true);
		replayMock(mockFactory);

		return mockFactory;
	}

	private void typeCharacter(char c) {
		recorderUnderTest.keyDown(c, tickingTime++);
		recorderUnderTest.keyUp(c, tickingTime++);
	}

	@Test
	public void constructorRequiresNonNullFactory() {

		try {
			new KeystrokeRecorderImpl(null);
			fail("should be impossible to instantiate recorder without a factory");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@Test
	public void constructorWorksWhenNonNullFactoryProvided() {

		KeystrokeFactory mockFactory = createMock(KeystrokeFactory.class);
		replayMock(mockFactory);
		KeystrokeRecorderImpl recorder = new KeystrokeRecorderImpl(
				mockFactory);
		assertNotNull(recorder);
	}

	@Test
	public void keyDownDefersCreationToFactory() {

		KeystrokeFactory mockFactory = createMockFactoryExpecting(testChar,
				testStart, new Keystroke(testChar, testStart));

		KeystrokeRecorder recorderUnderTest = new KeystrokeRecorderImpl(
				mockFactory);

		recorderUnderTest.keyDown(testChar, testStart);
	}

	@Test
	public void characterOnlyActiveBetweenKeyDownAndKeyUp() {
		assertFalse(recorderUnderTest.getActiveKeys().contains(testChar));

		recorderUnderTest.keyDown(testChar, testStart);
		assertTrue(recorderUnderTest.getActiveKeys().contains(testChar));

		recorderUnderTest.keyUp(testChar, testStart);
		assertFalse(recorderUnderTest.getActiveKeys().contains(testChar));
	}

	@Test
	public void successiveKeydownsAreIgnored() {

		// this mock will complain if the factory is consulted more than once
		KeystrokeFactory mockFactory = createMockFactoryExpecting(testChar,
				testStart, new Keystroke(testChar, testStart));

		KeystrokeRecorderImpl recorder = new KeystrokeRecorderImpl(
				mockFactory);

		recorder.keyDown(testChar, testStart);
		assertTrue(recorder.getActiveKeys().contains(testChar));

		for (int i = 0; i < 100; i++) {
			// if this resulted in creation of new keystroke, the mock would
			// choke
			recorder.keyDown(testChar, testStart);
		}

		assertTrue(recorder.getActiveKeys().contains(testChar));

	}

	@Test
	public void keyUpReleasesCreatedCharacter() {

		long releaseTime = testStart + 1000;

		Keystroke mockKeystroke = createMock(Keystroke.class);

		expect(mockKeystroke.getValue()).andReturn(testChar).anyTimes();

		mockKeystroke.release(releaseTime);
		expectLastCall().once();
		replayMock(mockKeystroke);

		KeystrokeFactory mockFactory = createMockFactoryExpecting(testChar,
				testStart, mockKeystroke);

		KeystrokeRecorder recorder = new KeystrokeRecorderImpl(
				mockFactory);

		recorder.keyDown(testChar, testStart);
		recorder.keyUp(testChar, releaseTime);
	}

	@Test
	public void typedCharactersAppearInResultSequence() {

		typeCharacter('a');

		assertEquals("a", recorderUnderTest.getResult().getCapturedValue());

		typeCharacter('a');

		assertEquals("aa", recorderUnderTest.getResult().getCapturedValue());

		typeCharacter('a');

		assertEquals("aaa", recorderUnderTest.getResult().getCapturedValue());
	}

}
