/*
 * Created on 2006/01/17
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
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.CustomAlphabetTable;

/**
 * A test class for the CustomAlphabet class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class CustomAlphabetTableTest {

	private Mockery context = new JUnit4Mockery();
	private Memory memory;
	private AlphabetTable alphabetTable;

	@Before
	public void setUp() throws Exception {
		memory = context.mock(Memory.class);
		alphabetTable = new CustomAlphabetTable(memory, 1000);
	}

	@Test
	public void testGetA0Char() {
		context.checking(new Expectations() {
			{
				one(memory).readUnsigned8(1000);
				will(returnValue((char) 3));
				one(memory).readUnsigned8(1006);
				will(returnValue((char) 2));
			}
		});
		assertEquals(3, alphabetTable.getA0Char((byte) 6));
		assertEquals(2, alphabetTable.getA0Char((byte) 12));
		assertEquals(' ', alphabetTable.getA0Char((byte) 0));
	}

	@Test
	public void testGetA1Char() {
		context.checking(new Expectations() {
			{
				one(memory).readUnsigned8(1026);
				will(returnValue((char) 3));
				one(memory).readUnsigned8(1032);
				will(returnValue((char) 2));
			}
		});
		assertEquals(3, alphabetTable.getA1Char((byte) 6));
		assertEquals(2, alphabetTable.getA1Char((byte) 12));
		assertEquals(' ', alphabetTable.getA1Char((byte) 0));
	}

	@Test
	public void testGetA2Char() {
		context.checking(new Expectations() {
			{
				one(memory).readUnsigned8(1052);
				will(returnValue((char) 3));
				one(memory).readUnsigned8(1058);
				will(returnValue((char) 2));
			}
		});
		assertEquals(3, alphabetTable.getA2Char((byte) 6));
		assertEquals(2, alphabetTable.getA2Char((byte) 12));
		assertEquals(' ', alphabetTable.getA2Char((byte) 0));
		assertEquals('\n', alphabetTable.getA2Char((byte) 7));
	}

	@Test
	public void testA0IndexOfNotFound() {
		context.checking(new Expectations() {
			{
				for (int i = 0; i < 26; i++) {
					one(memory).readUnsigned8(1000 + i);
					will(returnValue('a'));
				}
			}
		});
		assertEquals(-1, alphabetTable.getA0CharCode('@'));
	}

	@Test
	public void testA1IndexOfNotFound() {
		context.checking(new Expectations() {
			{
				for (int i = 0; i < 26; i++) {
					one(memory).readUnsigned8(1026 + i);
					will(returnValue('a'));
				}
			}
		});
		assertEquals(-1, alphabetTable.getA1CharCode('@'));
	}

	@Test
	public void testA2IndexOfNotFound() {
		context.checking(new Expectations() {
			{
				// char 7 is directly returned !!
				one(memory).readUnsigned8(1052);
				will(returnValue('a'));
				for (int i = 2; i < 26; i++) {
					one(memory).readUnsigned8(1052 + i);
					will(returnValue('a'));
				}
			}
		});
		assertEquals(-1, alphabetTable.getA2CharCode('@'));
	}
}
