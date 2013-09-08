package net.ladieu.biometrics.keystroke.controller;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.ladieu.biometrics.keystroke.controller.KeystrokeDirector;
import net.ladieu.biometrics.keystroke.controller.KeystrokeDirectorObserver;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification.KeystrokeNotificationType;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;
import net.ladieu.system.SystemTime;
import net.ladieu.system.TimeSource;
import net.ladieu.test.util.AbstractVerifyingMockObjectTest;

import org.junit.Before;
import org.junit.Test;

public class KeystrokeDirectorTest extends AbstractVerifyingMockObjectTest {

	private enum KeyState {
		DOWN, UP
	}

	private char FORCED_COMPLETION_CHAR = 'x';
	private KeystrokeDirector directorUnderTest;
	private FakeTogglingObserver fakeObserver;
	private long fixedMomentInTime = 55555;

	private static class FakeTogglingObserver implements
			KeystrokeDirectorObserver {
		public KeystrokeNotification lastNotification;
		public int count;

		public void notificationReceived(KeystrokeNotification notification) {
			count++;
			lastNotification = notification;
		}
	}

	@Before
	public void onSetUp() {
		directorUnderTest = new KeystrokeDirector(FORCED_COMPLETION_CHAR);
		fakeObserver = new FakeTogglingObserver();
		directorUnderTest.setRecorder(createNiceMock(KeystrokeRecorder.class));
		directorUnderTest.addObserver(fakeObserver);

		// force System clock to remain at fixed moment for testing
		SystemTime.setTimeSource(new TimeSource() {
			public long millis() {
				return fixedMomentInTime;
			}
		});

		assertFakeObserverState(0, null);
	}

	private KeyEvent keyEventFor(char c) {

		KeyEvent result = createNiceMock(KeyEvent.class);

		expect(result.getKeyChar()).andReturn(c).anyTimes();
		replayMock(result);

		return result;
	}

	private void applyActiveKeyExpectations(KeystrokeRecorder mockRecorder,
			Set<Character> activeKeys) {
		expect(mockRecorder.getActiveKeys()).andReturn(activeKeys);
	}

	private void applyKeystrokeExpectations(KeystrokeRecorder mockRecorder,
			KeyState keyState, char input, boolean returnValue) {

		if (KeyState.DOWN == keyState) {
			expect(mockRecorder.keyDown(input, fixedMomentInTime)).andReturn(
					returnValue).once();
		} else {
			expect(mockRecorder.keyUp(input, fixedMomentInTime)).andReturn(
					returnValue).once();
		}

	}

	/**
	 * Creates a mock KeystrokeRecorder that expects either keyDown or keyUp
	 * with the provided input, and will return the provided return value.
	 * 
	 * @param keyState
	 *            KeyState indicating if keyDown or keyUp is expected
	 * @param input
	 *            input character expected
	 * @param returnValue
	 *            desired return value
	 */
	private void setRecorderExpectations(KeyState keyState, char input,
			boolean returnValue) {

		KeystrokeRecorder mockRecorder = createMock(KeystrokeRecorder.class);

		applyKeystrokeExpectations(mockRecorder, keyState, input, returnValue);

		replayMock(mockRecorder);

		directorUnderTest.setRecorder(mockRecorder);
	}

	/**
	 * Combines setRecorderExpectations for keystroke info and active key info
	 */
	private void setRecorderExpectations(KeyState keyState, char input,
			boolean returnValue, Set<Character> activeKeys) {
		KeystrokeRecorder mockRecorder = createMock(KeystrokeRecorder.class);

		applyKeystrokeExpectations(mockRecorder, keyState, input, returnValue);
		applyActiveKeyExpectations(mockRecorder, activeKeys);

		replayMock(mockRecorder);

		directorUnderTest.setRecorder(mockRecorder);
	}

	private void assertFakeObserverState(int countValue,
			KeystrokeNotificationType expectedType) {
		assertEquals(countValue, fakeObserver.count);
		if (null == expectedType) {
			assertNull(fakeObserver.lastNotification);
		} else {
			assertEquals(expectedType, fakeObserver.lastNotification.getType());
		}
	}

	@Test
	public void notifyObserversInformsSubscribers() {

		directorUnderTest.notifyObservers(KeystrokeNotificationType.UPDATE);
		assertFakeObserverState(1, KeystrokeNotificationType.UPDATE);

		directorUnderTest.notifyObservers(KeystrokeNotificationType.CORRUPTION);
		assertFakeObserverState(2, KeystrokeNotificationType.CORRUPTION);
	}

	@Test
	public void notifyStopsNotifyingObserverAfterRemoved() {

		directorUnderTest.removeObserver(fakeObserver);

		assertFakeObserverState(0, null);
		directorUnderTest.notifyObservers(KeystrokeNotificationType.COMPLETION);

		assertFakeObserverState(0, null);
	}

	@Test
	public void keyPressedForwardsToRecorderAsKeyDown() {
		setRecorderExpectations(KeyState.DOWN, 'c', false);
		directorUnderTest.keyPressed(keyEventFor('c'));
	}

	@Test
	public void keyPressedForwardsToRecorderAsRelease() {
		setRecorderExpectations(KeyState.UP, 'd', false);
		directorUnderTest.keyReleased(keyEventFor('d'));
	}

	@Test
	public void keyPressedDoesNotSendUpdateEvenIfRecorderReportsChanges() {
		setRecorderExpectations(KeyState.DOWN, 'n', true);
		directorUnderTest.keyPressed(keyEventFor('n'));

		// observer shouldn't hear about a keydown event
		assertFakeObserverState(0, null);
	}

	@Test
	public void keyPressedDoesNotSendUpdateIfRecorderReportsNoChange() {
		setRecorderExpectations(KeyState.DOWN, 'z', false);
		assertFakeObserverState(0, null);
		directorUnderTest.keyPressed(keyEventFor('z'));

		// observer shouldn't hear about an event that doesn't create a state
		// change
		assertFakeObserverState(0, null);
	}

	@Test
	public void keyReleasedSendsUpdateIfRecorderReportsChanges() {
		setRecorderExpectations(KeyState.UP, 'm', true);
		directorUnderTest.keyReleased(keyEventFor('m'));
		assertFakeObserverState(1, KeystrokeNotificationType.UPDATE);
	}

	@Test
	public void keyReleasedDoesNotSendUpdateIfRecorderReportsNoChange() {
		setRecorderExpectations(KeyState.UP, 'r', false);
		assertFakeObserverState(0, null);
		directorUnderTest.keyReleased(keyEventFor('r'));

		// observer shouldn't hear about an event that doesn't create a state
		// change
		assertFakeObserverState(0, null);
	}

	@Test
	public void completionFlagToggledWhenCompletionTyped() {
		assertFalse(directorUnderTest.isInputCompleted());
		directorUnderTest.keyPressed(keyEventFor(FORCED_COMPLETION_CHAR));
		assertTrue(directorUnderTest.isInputCompleted());
	}

	@Test
	public void keyReleasedWillFireCompletionEventOnceActiveKeysIsEmpty() {

		// completion has been flagged (eg: by keydown of completion character)
		directorUnderTest.flagCompletion();
		assertFakeObserverState(0, null);

		// pretend 'c' is still active
		setRecorderExpectations(KeyState.UP, FORCED_COMPLETION_CHAR, false,
				Collections.singleton('c'));

		// pretend we released the completion character...
		directorUnderTest.keyReleased(keyEventFor(FORCED_COMPLETION_CHAR));
		assertFakeObserverState(0, null);

		// but completion only reported once c is released (and recorder reports
		// empty active key set)
		setRecorderExpectations(KeyState.UP, 'c', true,
				new HashSet<Character>());
		directorUnderTest.keyReleased(keyEventFor('c'));

		// note, we expect 2 events because the keyup of c will send an update
		assertFakeObserverState(2, KeystrokeNotificationType.COMPLETION);
	}

}
