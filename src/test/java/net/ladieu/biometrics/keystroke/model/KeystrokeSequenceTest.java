package net.ladieu.biometrics.keystroke.model;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class KeystrokeSequenceTest {

	private KeystrokeSequence sequenceUnderTest;
	private long tickingTime;

	@Before
	public void setUp() {
		tickingTime = 1;
		sequenceUnderTest = new KeystrokeSequence();
	}

	private void addKeystroke(char c) {
		Keystroke keystroke = new Keystroke(c, tickingTime++);
		sequenceUnderTest.addKeystroke(keystroke);
		keystroke.release(tickingTime++);
	}

	private void addKeystroke(char c, long startTime, long releaseTime) {
		Keystroke keystroke = new Keystroke(c, startTime);
		sequenceUnderTest.addKeystroke(keystroke);
		keystroke.release(releaseTime);
	}

	@Test
	public void sequenceCapturedValueStartsAsEmptyString() {
		assertEquals("", sequenceUnderTest.getCapturedValue());
	}

	@Test
	public void sequenceCapturedValueEqualsAccumulatedKeystrokes() {
		addKeystroke('a');
		assertEquals("a", sequenceUnderTest.getCapturedValue());

		addKeystroke('b');
		assertEquals("ab", sequenceUnderTest.getCapturedValue());

		addKeystroke('b');
		assertEquals("abb", sequenceUnderTest.getCapturedValue());
	}

	@Test
	public void iteratorContainsAccumulatedKeystrokes() {

		Iterator<Keystroke> iterator = sequenceUnderTest.iterator();

		assertFalse(iterator.hasNext());

		addKeystroke('a');

		iterator = sequenceUnderTest.iterator();

		assertTrue(iterator.hasNext());

		assertEquals('a', iterator.next().getValue());

		assertFalse(iterator.hasNext());

		addKeystroke('b');

		iterator = sequenceUnderTest.iterator();

		assertEquals('a', iterator.next().getValue());
		assertEquals('b', iterator.next().getValue());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void addKeystrokeSetsPriorIfNotFirst() {
		addKeystroke('a');
		addKeystroke('b');

		Iterator<Keystroke> iterator = sequenceUnderTest.iterator();

		Keystroke aKeystroke = iterator.next();
		Keystroke bKeystroke = iterator.next();

		assertNull(aKeystroke.getPrior());
		assertEquals(aKeystroke, bKeystroke.getPrior());
	}

	@Test
	public void addKeystrokeSetsNextOnPrior() {
		addKeystroke('a');
		addKeystroke('b');

		Keystroke aKeystroke = sequenceUnderTest.iterator().next();

		Keystroke lastKeystroke = sequenceUnderTest.getMostRecentKeystroke();

		assertEquals(lastKeystroke, aKeystroke.getNext());
	}

	@Test
	public void testDwellAndFlightTimesCapturedAccurately() {

		// each character captured 1 ms after the release of the prior keystroke
		addKeystroke('f', 1, 3); // flight 0, down 1, dwell 2, up 3
		addKeystroke('o', 2, 3); // flight -1, down 2, dwell 1, up 3
		addKeystroke('o', 3, 5); // flight 0, down 3, dwell 2, up 5
		addKeystroke('b', 7, 8); // flight 2, down 7, dwell 1, up 8
		addKeystroke('a', 8, 10); // flight 0, down 8, dwell 2, up 10
		addKeystroke('r', 9, 15); // flight -1, down 9, dwell 6, up 15

		Iterator<Keystroke> iterator = sequenceUnderTest.iterator();

		assertFlightAndDwell(iterator.next(), 0, 2); // f
		assertFlightAndDwell(iterator.next(), -1, 1); // o
		assertFlightAndDwell(iterator.next(), 0, 2); // o
		assertFlightAndDwell(iterator.next(), 2, 1); // b
		assertFlightAndDwell(iterator.next(), 0, 2); // a
		assertFlightAndDwell(iterator.next(), -1, 6); // r
	}

	@Test
	public void getMostRecentKeystrokeAlwaysReturnsLast() {

		assertNull(sequenceUnderTest.getMostRecentKeystroke());

		addKeystroke('a');
		assertEquals('a', sequenceUnderTest.getMostRecentKeystroke().getValue());
		addKeystroke('b');
		assertEquals('b', sequenceUnderTest.getMostRecentKeystroke().getValue());
	}
	
	@Test
	public void getFirstKeystrokeAlwaysReturnsFirst() {
		assertNull(sequenceUnderTest.getFirstKeystroke());
		
		addKeystroke('a');
		assertEquals('a', sequenceUnderTest.getFirstKeystroke().getValue());
		addKeystroke('b');
		assertEquals('a', sequenceUnderTest.getFirstKeystroke().getValue());
	}

	private void assertFlightAndDwell(Keystroke keystroke, long expectedFlight,
			long expectedDwell) {
		assertEquals(expectedDwell, keystroke.getDwellTime());
		assertEquals(expectedFlight, keystroke.getFlightTime());
	}
}
