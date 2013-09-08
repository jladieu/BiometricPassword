package net.ladieu.biometrics.keystroke.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.ladieu.biometrics.keystroke.model.Keystroke;
import net.ladieu.biometrics.keystroke.model.KeystrokeException;

import org.junit.Before;
import org.junit.Test;

public class KeystrokeTest {

	private Keystroke keystrokeUnderTest;
	private char keystrokeValue;
	private long startTime;

	@Before
	public void setUp() {
		keystrokeValue = 'c';
		startTime = 10;
		keystrokeUnderTest = new Keystroke(keystrokeValue, startTime);
	}

	@Test
	public void valueEqualsConstructorInput() {
		assertEquals("getValue() should return same as input", keystrokeValue,
				keystrokeUnderTest.getValue());
	}

	@Test
	public void startTimeEqualsConstructorInput() {
		assertEquals("startTime should be the same as input", startTime,
				keystrokeUnderTest.getStartTime());
	}

	@Test
	public void dwellTimeIsZeroBeforeRelease() {
		assertEquals(0L, keystrokeUnderTest.getDwellTime());
	}

	@Test
	public void dwellTimeIsDifferenceOfReleaseTimeOverStartTime() {

		long releaseTime = 15;
		long expectedDwellTime = releaseTime - startTime;

		keystrokeUnderTest.release(releaseTime);
		assertEquals(expectedDwellTime, keystrokeUnderTest.getDwellTime());
	}

	@Test
	public void dwellTimeCanNotBeNegative() {

		try {
			keystrokeUnderTest.release(startTime - 1);
			fail("negative dwelltime should not be possible");
		} catch (KeystrokeException e) {
			// pass, this should occur on bad release input
		}
	}

	@Test
	public void releaseOnlyAllowedOnce() {
		keystrokeUnderTest.release(startTime + 1);
		assertEquals(1L, keystrokeUnderTest.getDwellTime());
		try {
			keystrokeUnderTest.release(startTime + 2);
			fail("should not be possible to release twice");
		} catch (KeystrokeException e) {
			// pass, this should occur on second release
		}
	}

	@Test
	public void setPriorFailsIfPriorDidNotStartBefore() {
		long priorStartTime = startTime + 5;
		Keystroke prior = new Keystroke('d', priorStartTime);
		try {
			keystrokeUnderTest.setPrior(prior);
			fail("prior that started after current shouldn't be allowed");
		} catch (KeystrokeException e) {
			// pass, this should happen
		}
	}

	@Test
	public void setPriorFailsIfNullProvided() {
		try {
			keystrokeUnderTest.setPrior(null);
			fail("prior can't be explicitly set to null");
		} catch (IllegalArgumentException e) {
			// pass, this should happen
		}
	}

	@Test
	public void getPriorReturnsTheSetPrior() {
		Keystroke prior = new Keystroke('d', startTime - 1);

		keystrokeUnderTest.setPrior(prior);

		assertEquals(prior, keystrokeUnderTest.getPrior());
	}

	@Test
	public void setPriorOnlyAllowedOnce() {

		Keystroke prior = new Keystroke('d', 5);

		keystrokeUnderTest.setPrior(prior);

		try {
			keystrokeUnderTest.setPrior(prior);
			fail("setPrior should only be allowed once");
		} catch (KeystrokeException e) {
			// pass, this should happen
		}
	}

	@Test
	public void setNextFailsIfNextDidNotStartAfter() {
		long nextStartTime = startTime - 5;
		Keystroke next = new Keystroke('d', nextStartTime);
		try {
			keystrokeUnderTest.setNext(next);
			fail("next that started before current shouldn't be allowed");
		} catch (KeystrokeException e) {
			// pass, this should happen
		}
	}

	@Test
	public void setNextFailsIfNullProvided() {
		try {
			keystrokeUnderTest.setNext(null);
			fail("next can't be explicitly set to null");
		} catch (IllegalArgumentException e) {
			// pass, this should happen
		}
	}

	@Test
	public void getNextReturnsTheSetNext() {
		Keystroke next = new Keystroke('d', startTime + 1);
		keystrokeUnderTest.setNext(next);
		assertEquals(next, keystrokeUnderTest.getNext());
	}

	@Test
	public void setNextOnlyAllowedOnce() {
		Keystroke next = new Keystroke('d', 15);

		keystrokeUnderTest.setNext(next);

		try {
			keystrokeUnderTest.setNext(next);
			fail("setNext should only be allowed once");
		} catch (KeystrokeException e) {
			// pass, this should happen
		}
	}

	@Test
	public void flightTimeIsZeroIfNullPrior() {
		assertEquals(0L, keystrokeUnderTest.getFlightTime());
	}

	@Test
	public void flightTimeIsZeroIfPriorEndTimeNotYetKnown() {
		Keystroke prior = new Keystroke('d', 5);

		keystrokeUnderTest.setPrior(prior);

		assertEquals(0L, keystrokeUnderTest.getFlightTime());
	}

	@Test
	public void flightTimeIsDifferenceOfPriorEndTimeAndCurrentStartTime() {
		long priorStart = 5;
		long priorEnd = 7;
		Keystroke prior = new Keystroke('d', priorStart);
		prior.release(priorEnd);

		keystrokeUnderTest.setPrior(prior);

		assertEquals(startTime - priorEnd, keystrokeUnderTest.getFlightTime());
	}

	@Test
	public void flightTimeCanBeNegative() {

		long priorStart = 5;
		long priorEnd = 15;
		Keystroke prior = new Keystroke('d', priorStart);
		prior.release(priorEnd);

		keystrokeUnderTest.setPrior(prior);

		assertEquals(startTime - priorEnd, keystrokeUnderTest.getFlightTime());
		assertTrue(keystrokeUnderTest.getFlightTime() < 0);
	}

}
