/*
 * Copyright 2005-2009 by Wei-ju Wu
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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.zmpp.encoding.AlphabetElement;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.AlphabetTable.Alphabet;

/**
 * Test class for ZCharTranslator class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZCharTranslatorTest {

	private AlphabetTable alphabetTable;
	private ZCharTranslator translator;

	private AlphabetTable alphabetTableV2;
	private ZCharTranslator translatorV2;

	@Before
	public void setUp() {
		alphabetTable = new DefaultAlphabetTable();
		translator = new DefaultZCharTranslator(alphabetTable);
		alphabetTableV2 = new AlphabetTableV2();
		translatorV2 = new DefaultZCharTranslator(alphabetTableV2);
	}

	@Test
	public void testTranslate() {

		// Unknown
		assertEquals('?', translator.translate((char) 255));

		// alphabet 0
		assertEquals('a', translator.translate((char) 6));

		// Alphabet 1
		translator.translate((char) AlphabetTable.SHIFT_4);
		assertEquals('C', translator.translate((char) 8));

		// Alphabet 2
		translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals('2', translator.translate((char) 10));

		// Alphabet 2, NEWLINE
		translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals('\n', translator.translate((char) 7));
	}

	@Test
	public void test0IsSpace() {

		assertEquals(' ', translator.translate((char) 0));
		translator.translate((char) AlphabetTable.SHIFT_4);
		assertEquals(' ', translator.translate((char) 0));
		translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals(' ', translator.translate((char) 0));

		assertEquals(' ', translatorV2.translate((char) 0));
		translatorV2.translate((char) AlphabetTable.SHIFT_4);
		assertEquals(' ', translatorV2.translate((char) 0));
		translatorV2.translate((char) AlphabetTable.SHIFT_5);
		assertEquals(' ', translatorV2.translate((char) 0));
	}

	// **********************************************************************
	// ***** Shift
	// ******************************************

	@Test
	public void testShiftFromA0() {

		char c = translator.translate((char) AlphabetTable.SHIFT_4);
		assertEquals('\0', c);
		assertEquals(Alphabet.A1, translator.getCurrentAlphabet());

		translator.reset();
		assertEquals(Alphabet.A0, translator.getCurrentAlphabet());

		c = translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals('\0', c);
		assertEquals(Alphabet.A2, translator.getCurrentAlphabet());
	}

	@Test
	public void testShiftFromA1() {

		// Switch to A1
		char c = translator.translate((char) AlphabetTable.SHIFT_4);

		c = translator.translate((char) AlphabetTable.SHIFT_4);
		assertEquals('\0', c);
		assertEquals(Alphabet.A2, translator.getCurrentAlphabet());

		// Switch to A1 again
		translator.reset();
		c = translator.translate((char) AlphabetTable.SHIFT_4);

		c = translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals('\0', c);
		assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
	}

	@Test
	public void testShiftFromA2() {

		// Switch to A2
		char c = translator.translate((char) AlphabetTable.SHIFT_5);

		c = translator.translate((char) AlphabetTable.SHIFT_4);
		assertEquals('\0', c);
		assertEquals(Alphabet.A0, translator.getCurrentAlphabet());

		// Switch to A2 again
		translator.reset();
		c = translator.translate((char) AlphabetTable.SHIFT_5);

		c = translator.translate((char) AlphabetTable.SHIFT_5);
		assertEquals('\0', c);
		assertEquals(Alphabet.A1, translator.getCurrentAlphabet());
	}

	/**
	 * The default alphabet table should reset to A0 after retrieving a code.
	 */
	@Test
	public void testImplicitReset() {

		translator.translate((char) AlphabetTable.SHIFT_4);
		translator.translate((char) 7);
		assertEquals(Alphabet.A0, translator.getCurrentAlphabet());

		translator.translate((char) AlphabetTable.SHIFT_5);
		translator.translate((char) 7);
		assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
	}

	@Test
	public void testGetAlphabetElement() {

		// Alphabet A0
		AlphabetElement elem1 = translator.getAlphabetElementFor('c');
		assertEquals(Alphabet.A0, elem1.getAlphabet());
		assertEquals(8, elem1.getZCharCode());

		AlphabetElement elem1b = translator.getAlphabetElementFor('a');
		assertEquals(Alphabet.A0, elem1b.getAlphabet());
		assertEquals(6, elem1b.getZCharCode());

		AlphabetElement elem2 = translator.getAlphabetElementFor('d');
		assertEquals(Alphabet.A0, elem2.getAlphabet());
		assertEquals(9, elem2.getZCharCode());

		// Alphabet A1
		AlphabetElement elem3 = translator.getAlphabetElementFor('C');
		assertEquals(Alphabet.A1, elem3.getAlphabet());
		assertEquals(8, elem3.getZCharCode());

		// Alphabet A2
		AlphabetElement elem4 = translator.getAlphabetElementFor('#');
		assertEquals(Alphabet.A2, elem4.getAlphabet());
		assertEquals(23, elem4.getZCharCode());

		// ZSCII code
		AlphabetElement elem5 = translator.getAlphabetElementFor('@');
		assertEquals(null, elem5.getAlphabet());
		assertEquals(64, elem5.getZCharCode());

		// Newline is tricky, this is always A2/7 !!!
		AlphabetElement newline = translator.getAlphabetElementFor('\n');
		assertEquals(Alphabet.A2, newline.getAlphabet());
		assertEquals(7, newline.getZCharCode());
	}

	// **********************************************************************
	// ***** Shifting in V2
	// ******************************************

	@Test
	public void testShiftV2FromA0() {

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
		translatorV2.reset();

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
		translatorV2.reset();

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
		translatorV2.reset();

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftV2FromA1() {

		translatorV2.translate((char) AlphabetTable.SHIFT_2);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_2);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_2);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_2);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftV2FromA2() {

		translatorV2.translate((char) AlphabetTable.SHIFT_3);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_3);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_3);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		translatorV2.translate((char) AlphabetTable.SHIFT_3);

		assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftNotLocked() {

		translatorV2.translate((char) AlphabetTable.SHIFT_2);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());

		translatorV2.translate((char) AlphabetTable.SHIFT_3);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftNotLockedChar0() {

		translatorV2.translate((char) AlphabetTable.SHIFT_2);
		translatorV2.translate((char) 0);
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());

		translatorV2.translate((char) AlphabetTable.SHIFT_3);
		translatorV2.translate((char) 0);
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftLocked() {

		translatorV2.translate((char) AlphabetTable.SHIFT_4);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
		translatorV2.reset();
		assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());

		translatorV2.translate((char) AlphabetTable.SHIFT_5);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
	}

	/**
	 * Test if the shift lock is reset after the a non-locking shift was met.
	 */
	@Test
	public void testShiftLockSequenceLock1() {

		translatorV2.translate((char) AlphabetTable.SHIFT_4);
		translatorV2.translate((char) AlphabetTable.SHIFT_2);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
	}

	@Test
	public void testShiftLockSequenceLock2() {

		translatorV2.translate((char) AlphabetTable.SHIFT_5);
		translatorV2.translate((char) AlphabetTable.SHIFT_3);
		translatorV2.translate((char) 10);
		assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
	}
}
