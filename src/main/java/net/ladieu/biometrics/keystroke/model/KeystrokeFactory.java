package net.ladieu.biometrics.keystroke.model;



/**
 * Interface to expose creation of Keystroke characters.
 * 
 * @author jrladieu
 */
public interface KeystrokeFactory {

	/**
	 * @return true if the given character is accepted by the factory
	 */
	public boolean acceptsCharacter(char c);

	/**
	 * Creates a Keystroke for the given character
	 * 
	 * @param c
	 *            character for which to create a keystroke
	 * @param startTime
	 *            the time the keystroke was begun
	 * @return a Keystroke representing the given character with the provided
	 *         startTime
	 * @throws IllegalArgumentException
	 *             if the provided keystroke is not a valid character
	 * @throws IllegalArgumentException
	 *             if the provided startTime is invalid
	 */
	public Keystroke createKeystroke(char c, long startTime);

}
