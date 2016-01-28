/*
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package test.zmpp.encoding;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.base.Memory;
import org.zmpp.encoding.CustomAccentTable;

/**
 * Test class for CustomAccentTable.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
@RunWith(JMock.class)
public class CustomAccentTableTest {
	private static final int ADDRESS = 4711;
	private Mockery context = new JUnit4Mockery();
	private Memory memory = context.mock(Memory.class);
	private CustomAccentTable accentTable, noAccentTable;

	@Before
	public void setUp() throws Exception {
		accentTable = new CustomAccentTable(memory, ADDRESS);
		noAccentTable = new CustomAccentTable(memory, 0);
	}

	@Test
	public void testGetLengthNoTable() {
		assertEquals(0, noAccentTable.getLength());
	}

	@Test
	public void testGetLength() {
		context.checking(new Expectations() {
			{
				one(memory).readUnsigned8(ADDRESS);
				will(returnValue((char) 3));
			}
		});
		assertEquals(3, accentTable.getLength());
	}

	@Test
	public void testGetAccentNoTable() {
		assertEquals('?', noAccentTable.getAccent(42));
	}

	@Test
	public void testGetAccent() {
		context.checking(new Expectations() {
			{
				one(memory).readUnsigned16(ADDRESS + 7);
				will(returnValue('^'));
			}
		});
		assertEquals('^', accentTable.getAccent(3));
	}

	@Test
	public void testGetIndexOfLowerCase() {
		context.checking(new Expectations() {
			{
				// length
				atLeast(1).of(memory).readUnsigned8(ADDRESS);
				will(returnValue((char) 80));
				// reference character
				one(memory).readUnsigned16(ADDRESS + 2 * 6 + 1);
				will(returnValue('B'));

				one(memory).readUnsigned16(ADDRESS + 1);
				will(returnValue('a'));
				one(memory).readUnsigned16(ADDRESS + 2 + 1);
				will(returnValue('b'));
			}
		});
		assertEquals(1, accentTable.getIndexOfLowerCase(6));
	}

	@Test
	public void testGetIndexOfLowerCaseNotFound() {
		context.checking(new Expectations() {
			{
				// length
				atLeast(1).of(memory).readUnsigned8(ADDRESS);
				will(returnValue((char) 2));
				// reference character
				atLeast(1).of(memory).readUnsigned16(ADDRESS + 2 * 1 + 1);
				will(returnValue('^'));

				one(memory).readUnsigned16(ADDRESS + 1);
				will(returnValue('a'));
			}
		});
		assertEquals(1, accentTable.getIndexOfLowerCase(1));
	}
}
