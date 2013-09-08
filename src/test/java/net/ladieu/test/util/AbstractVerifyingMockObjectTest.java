package net.ladieu.test.util;

import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

/**
 * An abstract test superclass intended to automate the common usage of
 * verifiable mock objects. Any mock created using the createNewMock() method
 * will be automatically verified at the beginning of tearDown.
 * 
 * @author <a href="jladieu@sermo.com">Josh Ladieu</a>
 */
public abstract class AbstractVerifyingMockObjectTest {

	private List<Object> mocksToVerify;

	public AbstractVerifyingMockObjectTest() {
		super();
	}

	@Before
	public final void setUp() throws Exception {
		mocksToVerify = new ArrayList<Object>();
		onSetUp();
	}

	/**
	 * Override in subclass to provide custom setup.
	 */
	protected void onSetUp() throws Exception {
		// no-op, available for override
	}

	@After
	public final void tearDown() throws Exception {
		onTearDown();
		verifyMocks();
		mocksToVerify = null;
	}

	/**
	 * Override in subclass to provide custom teardown.
	 */
	protected void onTearDown() throws Exception {
		// no-op, available for override
	}

	private void verifyMocks() {
		for (Object currentMock : mocksToVerify) {
			verify(currentMock);
		}
	}

	protected void replayMock(Object mock) {
		replay(mock);
		mocksToVerify.add(mock);
	}

}
