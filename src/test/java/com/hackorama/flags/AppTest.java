package com.hackorama.flags;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for App TODO
 * 
 * @author Kishan Thomas (kishan.thomas@gmail.com)
 *
 */
public class AppTest extends TestCase {
	
	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	public void testApp() {
		assertTrue(true);
	}
}
