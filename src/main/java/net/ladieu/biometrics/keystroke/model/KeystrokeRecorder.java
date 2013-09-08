package net.ladieu.biometrics.keystroke.model;

import java.util.Set;


public interface KeystrokeRecorder {

	/**
	 * Registers a keyDown event for the given character at the given time
	 * 
	 * @param c
	 *            the character that was pressed
	 * @param time
	 *            the time of the event
	 * @return true if this results in a change of recorded state, or false if
	 *         the key event is ignored
	 */
	boolean keyDown(char c, long time);

	/**
	 * Registers a keyUp event for the given character at the given time
	 * 
	 * @param c
	 *            the character that was released
	 * @param time
	 *            the time of the event
	 * @return true if this results in a change of recorded state, or false if
	 *         the key event is ignored
	 */
	boolean keyUp(char c, long time);

	/**
	 * @return the accumulated KeystrokeSequence since the last reset
	 */
	KeystrokeSequence getResult();

	/**
	 * The Set of characters currently considered pressed/down by the recorder.
	 * 
	 * @return a Set of characters representing the active keys
	 */
	Set<Character> getActiveKeys();

	/**
	 * Returns the state of the recorder back to new
	 */
	void reset();
}
